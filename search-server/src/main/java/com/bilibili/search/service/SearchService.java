package com.bilibili.search.service;

import com.bilibili.common.domain.api.pojo.RecommendVideo;
import com.bilibili.common.domain.search.dto.EsIndexDTO;
import com.bilibili.common.domain.search.dto.EsKeywordDTO;
import com.bilibili.common.domain.search.vo.TotalCountSearchVO;
import com.bilibili.common.domain.search.vo.UserKeyWordSearchVO;
import com.bilibili.common.domain.search.vo.VideoKeywordSearchVO;
import com.bilibili.common.util.Result;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;

public interface SearchService {
    Result<TotalCountSearchVO> totalKeywordSearch(String keyword);

    Result<List<VideoKeywordSearchVO>> videoPageKeywordSearch(String keyword, Integer pageNumber, Integer type) throws JsonProcessingException;

    Result<List<UserKeyWordSearchVO>> userPageKeywordSearch(String keyword, Integer pageNumber, Integer type, Integer userId) throws JsonProcessingException;

    Result<List<String>> likelyKeywordSearch(String keyword) throws IOException;

    List<RecommendVideo> likelyVideoRecommend(String videoId) throws IOException;

    Result<Boolean> addKeywordSearchRecord(EsKeywordDTO esKeywordDTO) throws IOException;

    void createIndex(EsIndexDTO esIndexDTO);

    void deleteIndex(String indexName) throws IOException;
}
