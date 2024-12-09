package com.bilibili.video.controller;

import com.bilibili.common.domain.search.entity.UserEntity;
import com.bilibili.common.domain.search.vo.VideoKeywordSearchVO;
import com.bilibili.common.domain.user.entity.User;
import com.bilibili.common.domain.video.entity.audience_reactions.Comment;
import com.bilibili.common.domain.video.entity.video_production.Video;
import com.bilibili.common.domain.video.entity.video_production.VideoData;
import com.bilibili.video.service.GetTableDataService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/video/getTableData")
@Api(tags = "被远程调用以查询数据库相关接口")
@Slf4j
public class GetTableDataController {

    @Resource
    GetTableDataService getTableDataService;

    @GetMapping("/selectVideoJoinCount")
    public long selectVideoJoinCount(MPJLambdaWrapper<Video> videoLikeLambdaQueryWrapper){
        return getTableDataService.selectVideoJoinCount(videoLikeLambdaQueryWrapper);
    }

    @GetMapping("/selectCommentJoinCount")
    public long selectCommentJoinCount(MPJLambdaWrapper<Comment> commentLikeLambdaQueryWrapper){
        return getTableDataService.selectCommentJoinCount(commentLikeLambdaQueryWrapper);
    }

    @GetMapping("/updateById")
    public void updateById(Video video){
        getTableDataService.updateById(video);
    }

    @GetMapping("/selectVideoJoinList")
    public List<VideoKeywordSearchVO> selectVideoJoinList (Class<VideoKeywordSearchVO> var1, MPJLambdaWrapper<Video> wrapper){
        return getTableDataService.selectVideoJoinList(var1,wrapper);
    }

    @GetMapping("/selectVideoList")
    public List<VideoData> selectList(MPJLambdaWrapper<VideoData> wrapper){
        return getTableDataService.selectList(wrapper);
    }
}
