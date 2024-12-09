package com.bilibili.video.service;

import com.bilibili.common.domain.search.vo.VideoKeywordSearchVO;
import com.bilibili.common.domain.video.entity.audience_reactions.Comment;
import com.bilibili.common.domain.video.entity.video_production.Video;
import com.bilibili.common.domain.video.entity.video_production.VideoData;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.List;

public interface GetTableDataService {
    long selectVideoJoinCount(MPJLambdaWrapper<Video> videoLikeLambdaQueryWrapper);

    long selectCommentJoinCount(MPJLambdaWrapper<Comment> commentLikeLambdaQueryWrapper);

    void updateById(Video video);

    List<VideoKeywordSearchVO> selectVideoJoinList(Class<VideoKeywordSearchVO> var1, MPJLambdaWrapper<Video> wrapper);

    List<VideoData> selectList(MPJLambdaWrapper<VideoData> wrapper);
}
