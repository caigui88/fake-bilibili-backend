package com.bilibili.search.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bilibili.api.client.UserClient;
import com.bilibili.common.domain.api.pojo.RecommendVideo;
import com.bilibili.common.domain.search.dto.EsIndexDTO;
import com.bilibili.common.domain.search.dto.EsKeywordDTO;
import com.bilibili.common.domain.search.vo.TotalCountSearchVO;
import com.bilibili.common.domain.search.vo.UserKeyWordSearchVO;
import com.bilibili.common.domain.search.vo.VideoKeywordSearchVO;
import com.bilibili.common.domain.user.entity.Follow;
import com.bilibili.common.util.Result;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import com.bilibili.search.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static com.bilibili.common.constant.SearchConstant.*;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Resource
    public RestHighLevelClient client;

    final int size = 20;

    @Resource
    UserClient userClient;

    @Resource
    ObjectMapper objectMapper;

    /**
     * 获取用户、视频总匹配文档数和页数
     * @param keyword
     * @return
     */
    @Override
    public Result<TotalCountSearchVO> totalKeywordSearch(String keyword) {
        // 视频搜索请求构建
        SearchRequest videoSearchRequest = new SearchRequest(VIDEO_INDEX_NAME);
        SearchSourceBuilder videoSearchSourceBuilder = new SearchSourceBuilder();
        videoSearchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, MULTI_QUERY_VIDEO_NAME, MULTI_QUERY_AUTHOR_NAME, MULTI_QUERY_INTRO)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS));
        videoSearchSourceBuilder.minScore(1.0f);
        videoSearchSourceBuilder.sort(ORDER_BY_SCORE, SortOrder.DESC);
        videoSearchSourceBuilder.fetchSource(true);
        videoSearchRequest.source(videoSearchSourceBuilder);

        // 用户搜索请求构建
        SearchRequest userSearchRequest = new SearchRequest(USER_INDEX_NAME);
        SearchSourceBuilder userSearchSourceBuilder = new SearchSourceBuilder();
        userSearchSourceBuilder.minScore(1.0f);
        userSearchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, MULTI_QUERY_NICKNAME, MULTI_QUERY_INTRO)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS));
        userSearchSourceBuilder.sort(ORDER_BY_SCORE, SortOrder.DESC);
        userSearchSourceBuilder.fetchSource(true);
        userSearchRequest.source(userSearchSourceBuilder);

        // 执行搜索
        SearchResponse videoResponse;
        SearchResponse userResponse;
        try {
            videoResponse = client.search(videoSearchRequest, RequestOptions.DEFAULT);
            userResponse = client.search(userSearchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 处理搜索响应
        long totalVideoCount = videoResponse.getHits().getTotalHits().value;
        long totalVideoPages = totalVideoCount / size + (totalVideoCount % size == 0 ? 0 : 1);
        long totalUserCount = userResponse.getHits().getTotalHits().value;
        long totalUserPages = totalUserCount / size + (totalUserCount % size == 0 ? 0 : 1);

        return Result.data(new TotalCountSearchVO().setTotalVideoPage(totalVideoPages).setTotalVideoNum(totalVideoCount)
                .setTotalUserNum(totalUserCount).setTotalUserPage(totalUserPages));
    }

    /**
     * 视频分页搜索及获取
     * @param keyword
     * @param pageNumber
     * @param type
     * @return
     */
    @Override
    public Result<List<VideoKeywordSearchVO>> videoPageKeywordSearch(String keyword, Integer pageNumber, Integer type) throws JsonProcessingException {
        List<VideoKeywordSearchVO> videoKeywordSearchVOS = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(VIDEO_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, MULTI_QUERY_VIDEO_NAME, MULTI_QUERY_AUTHOR_NAME, MULTI_QUERY_INTRO)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS));
        searchSourceBuilder.minScore(1.0f);
        if (type == 0) {
            searchSourceBuilder.sort(ORDER_BY_SCORE, SortOrder.DESC);
        } else if (type == 1) {
            searchSourceBuilder.sort(ORDER_BY_PLAY_COUNT, SortOrder.DESC);
        } else if (type == 2) {
            searchSourceBuilder.sort(ORDER_BY_CREATE_TIME, SortOrder.DESC);
        } else if (type == 3) {
            searchSourceBuilder.sort(ORDER_BY_DANMAKU_COUNT, SortOrder.DESC);
        } else {
            searchSourceBuilder.sort(ORDER_BY_COLLECT_COUNT, SortOrder.DESC);
        }

        searchSourceBuilder.fetchSource(true);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;
        searchSourceBuilder.size(size);
        searchSourceBuilder.from((pageNumber - 1) * size);
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (SearchHit hit : response.getHits().getHits()) {
            videoKeywordSearchVOS.add(objectMapper.readValue(hit.getSourceAsString(), VideoKeywordSearchVO.class));
        }
        return Result.data(videoKeywordSearchVOS);
    }

    /**
     * 用户分页搜索及获取
     * @param keyword
     * @param pageNumber
     * @param type
     * @param userId
     * @return
     */
    @Override
    public Result<List<UserKeyWordSearchVO>> userPageKeywordSearch(String keyword, Integer pageNumber, Integer type, Integer userId) throws JsonProcessingException {
        List<UserKeyWordSearchVO> userKeyWordSearchResponses = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(USER_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, MULTI_QUERY_NICKNAME, MULTI_QUERY_INTRO)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS));
        if (type == 0) {
            searchSourceBuilder.sort(ORDER_BY_SCORE, SortOrder.DESC);
        } else if (type == 1) {
            searchSourceBuilder.sort(ORDER_BY_FANS_COUNT, SortOrder.DESC);
        } else {
            searchSourceBuilder.sort(ORDER_BY_FANS_COUNT, SortOrder.ASC);
        }
        searchSourceBuilder.fetchSource(true);
        searchRequest.source(searchSourceBuilder);
        List<Integer> ids = new ArrayList<>();
        SearchResponse response;

        // 分页处理
        searchSourceBuilder.size(size);
        searchSourceBuilder.from((pageNumber - 1) * size);

        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 遍历搜索结果并转换为 UserKeyWordSearchVO 对象存储
        for (SearchHit hit : response.getHits().getHits()) {
            ids.add( Integer.valueOf((String) hit.getSourceAsMap().get(INDEX_ID)));
            userKeyWordSearchResponses.add(objectMapper.readValue(hit.getSourceAsString(), UserKeyWordSearchVO.class));
        }
        Set<Integer> followSet = new HashSet(10);
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFansId, userId);
        List<Follow> follows = userClient.selectFollowList(followLambdaQueryWrapper);
        for (Follow follow : follows) {
            followSet.add(follow.getIdolId());
        }
        userKeyWordSearchResponses.forEach(userKeyWordSearchResponse -> {
            if (followSet.contains(userKeyWordSearchResponse.getId())) {
                userKeyWordSearchResponse.setIsFollow(true);
            } else {
                userKeyWordSearchResponse.setIsFollow(false);
            }
        });
        return Result.data(userKeyWordSearchResponses);
    }

    /**
     * 搜索但未确认时类似关键字查询
     * @param keyword
     * @return
     * @throws IOException
     */
    @Override
    public Result<List<String>> likelyKeywordSearch(String keyword) throws IOException {
        SearchRequest searchRequest = new SearchRequest(HISTORY_SEARCH_INDEX_NAME);
        List<String> list = new ArrayList<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, MULTI_QUERY_SEARCH_WORD));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            EsKeywordDTO esKeywordDTO = objectMapper.readValue(searchHit.getSourceAsString(), EsKeywordDTO.class);
            list.add(esKeywordDTO.getSearchWord());
        }
        return Result.data(list);
    }

    /**
     * 视频推荐查询
     * @param videoId
     * @return
     * @throws IOException
     */
    @Override
    public List<RecommendVideo> likelyVideoRecommend(String videoId) throws IOException {
        //使用了特定查询query--morelikethisquery来强化对关键字匹配度的查询
        SearchRequest searchRequest = new SearchRequest(VIDEO_INDEX_NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        Item[] items = {
                new Item(VIDEO_INDEX_NAME, videoId),
        };
        sourceBuilder.sort(ORDER_BY_SCORE, SortOrder.DESC);
        sourceBuilder.query(QueryBuilders.moreLikeThisQuery(
                        null,
                        null,
                        items
                ).minTermFreq(1)
                .maxQueryTerms(12));
        List<RecommendVideo> list = new ArrayList<>();
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            RecommendVideo recommendVideo = objectMapper.readValue(searchHit.getSourceAsString(), RecommendVideo.class);
            if (!recommendVideo.getVideoId().equals(videoId)) {
                list.add(recommendVideo);
            }
        }
        return list;
    }

    /**
     * 添加搜索记录
     * @param esKeywordDTO
     * @return
     * @throws IOException
     */
    @Override
    public Result<Boolean> addKeywordSearchRecord(EsKeywordDTO esKeywordDTO) throws IOException {
        IndexRequest indexRequest = new IndexRequest(HISTORY_SEARCH_INDEX_NAME);
        indexRequest.source(objectMapper.convertValue(esKeywordDTO, Map.class), XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
        return Result.success(true);
    }

    /**
     * 创建索引
     * @param esIndexDTO
     */
    @Override
    public void createIndex(EsIndexDTO esIndexDTO) {
        try {
            String index = esIndexDTO.getIndexName();
            Map<String, String> map = esIndexDTO.getProperties();
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.startObject(INDEX_START_OBJECT_MAPPINGS);
                {
                    builder.startObject(INDEX_START_OBJECT_PROPERTIES);
                    {
                        // 添加其他字段
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            builder.startObject(entry.getKey());
                            {
                                builder.field(INDEX_FIELD_TYPE, entry.getValue());
                            }
                            builder.endObject();
                        }
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
            CreateIndexRequest request = new CreateIndexRequest(index);
            request.source(builder);
            client.indices().create(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除索引
     * @param indexName
     * @throws IOException
     */
    @Override
    public void deleteIndex(String indexName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (exists) {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            client.indices().delete(request, RequestOptions.DEFAULT);
            System.out.println("索引已删除");
        } else {
            System.out.println("索引不存在");
        }

    }
}
