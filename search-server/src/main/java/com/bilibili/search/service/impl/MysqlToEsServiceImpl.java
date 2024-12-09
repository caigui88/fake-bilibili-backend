package com.bilibili.search.service.impl;

import com.bilibili.api.client.UserClient;
import com.bilibili.api.client.VideoClient;
import com.bilibili.common.constant.SearchConstant;
import com.bilibili.common.domain.search.entity.UserEntity;
import com.bilibili.common.domain.search.vo.VideoKeywordSearchVO;
import com.bilibili.common.domain.user.entity.IdCount;
import com.bilibili.common.domain.user.entity.User;
import com.bilibili.common.domain.video.entity.video_production.Video;
import com.bilibili.common.domain.video.entity.video_production.VideoData;
import com.bilibili.search.service.MysqlToEsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MysqlToEsServiceImpl implements MysqlToEsService {

    @Resource
    ObjectMapper objectMapper;

    @Resource
    RestHighLevelClient client;

    @Resource
    UserClient userClient;

    @Resource
    VideoClient videoClient;

    /**
     * 将用户数据从mysql同步到es
     * @return
     * @throws IOException
     */
    @Override
    public Boolean userMysqlToEs() throws IOException {
        //查询出所有用户并转换成map插入es
        MPJLambdaWrapper<User> wrapper = new MPJLambdaWrapper<>();
        List<Map<String, Object>> userMap = new ArrayList<>();
        wrapper.select(User::getCover, User::getNickname, User::getId, User::getIntro);
        List<UserEntity> userList = userClient.selectJoinList(UserEntity.class, wrapper);
        for (UserEntity user : userList) {
            userMap.add(objectMapper.convertValue(user,Map.class));
        }
        for (Map<String, Object> map : userMap) {
            IndexRequest indexRequest = new IndexRequest(SearchConstant.USER_INDEX_NAME);
            Integer id = (Integer) map.get(SearchConstant.INDEX_ID);
            indexRequest.id(id.toString());
            indexRequest.source(map, XContentType.JSON);
            client.index(indexRequest, RequestOptions.DEFAULT);
        }
        return true;
    }

    /**
     * 将视频数据从mysql同步到es
     * @return
     * @throws IOException
     */
    @Override
    public Boolean videoMysqlToEs() throws IOException {
        //查询出所有视频并转换成map插入es
        MPJLambdaWrapper<Video> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(User.class, User::getId, Video::getUserId);
        wrapper.leftJoin(VideoData.class, VideoData::getVideoId, Video::getId);
        wrapper.select(Video::getVideoCover, Video::getIntro, Video::getCreateTime, Video::getLength, Video::getUrl);
        wrapper.select(VideoData::getDanmakuCount, VideoData::getPlayCount);
        wrapper.selectAs(Video::getName, VideoKeywordSearchVO::getVideoName);
        wrapper.selectAs(User::getNickname, VideoKeywordSearchVO::getAuthorName);
        wrapper.selectAs(Video::getId, VideoKeywordSearchVO::getVideoId);
        wrapper.selectAs(User::getId, VideoKeywordSearchVO::getAuthorId);
        List<VideoKeywordSearchVO> list = videoClient.selectVideoJoinList(VideoKeywordSearchVO.class, wrapper);
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (VideoKeywordSearchVO response : list) {
            mapList.add(objectMapper.convertValue(response, Map.class));
        }
        for (Map<String, Object> map : mapList) {
            IndexRequest indexRequest = new IndexRequest(SearchConstant.VIDEO_INDEX_NAME);
            indexRequest.id(String.valueOf(map.get(SearchConstant.VIDEO_INDEX_ID)));
            indexRequest.source(map, XContentType.JSON);
            client.index(indexRequest, RequestOptions.DEFAULT);
        }
        updateUserData();
        return true;
    }

    /**
     * 将视频数据从mysql同步到es
     * @return
     * @throws IOException
     */
    @Override
    public Boolean updateVideoData() throws IOException {
        //查询出所有视频绑定的相关数据比如播放数评论数弹幕数然后插入es
        BulkRequest bulkRequest = new BulkRequest();
        List<VideoData> videoDataList = videoClient.selectList(null);
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (VideoData videoData : videoDataList) {
            Map<String,Object> map=objectMapper.convertValue(videoData,Map.class);
            map.remove(SearchConstant.INDEX_ID);
            map.remove(SearchConstant.VIDEO_INDEX_REMOVE_COMMENT_COUNT);
            map.remove(SearchConstant.VIDEO_INDEX_REMOVE_LIKE_COUNT);
            mapList.add(map);
        }
        for (Map<String, Object> map : mapList) {
            int intId = (Integer) map.get(SearchConstant.VIDEO_INDEX_ID);
            String id = String.valueOf(intId);
            bulkRequest.add(new UpdateRequest(SearchConstant.VIDEO_INDEX_NAME, id).doc(map));
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return true;
    }

    /**
     * 将用户数据从mysql同步到es
     * @return
     * @throws IOException
     */
    @Override
    public Boolean updateUserData() throws IOException {
        //查询出所有用户的相关数据比如关注数粉丝数然后插入es
        BulkRequest bulkRequest = new BulkRequest();
        List<User> userList = userClient.selectList(null);
        List<Integer> ids = new ArrayList<>();
        for (User user : userList) {
            ids.add(user.getId());
        }
        Map<Integer, Integer> fansCountMap = new HashMap<>();
        Map<Integer, Integer> videoCountMap = new HashMap<>();
        List<IdCount> idCountList = userClient.getIdolCount(ids);
        List<IdCount> videoCountList=userClient.getVideoCount(ids);
        for (IdCount idCount : idCountList) {
            fansCountMap.put(idCount.getId(), idCount.getCount());
        }
        for(IdCount idCount : videoCountList){
            videoCountMap.put(idCount.getId(),idCount.getCount());
        }
        for (Integer id : ids) {
            Map<String, Object> map = new HashMap<>();
            map.put(SearchConstant.INDEX_ID, id.toString());
            map.put(SearchConstant.USER_INDEX_PUT_FANS_COUNT, fansCountMap.getOrDefault(id, 0));
            map.put(SearchConstant.USER_INDEX_PUT_VIDEO_COUNT, videoCountMap.getOrDefault(id, 0));
            bulkRequest.add(new UpdateRequest(SearchConstant.USER_INDEX_NAME, id.toString()).doc(map));
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return true;
    }
}
