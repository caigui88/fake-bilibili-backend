package com.bilibili.api.client;

import com.bilibili.common.domain.api.pojo.UploadVideo;
import com.bilibili.common.domain.search.vo.VideoKeywordSearchVO;
import com.bilibili.common.domain.video.entity.audience_reactions.Comment;
import com.bilibili.common.domain.video.entity.video_production.Video;
import com.bilibili.common.domain.video.entity.video_production.VideoData;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@FeignClient(name = "video-service",url = "http://localhost:8223")
public interface VideoClient {

    @PostMapping(value = "/videoEncode/uploadVideo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadVideo(@RequestPart("multipartFile") MultipartFile multipartFile);

    @PostMapping("/videoEncode/getVideoInputStream")
    ResponseEntity<Resource> getVideo(@RequestBody UploadVideo uploadVideo);

    @PostMapping(value = "/videoEncode/uploadVideoCover",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadVideoCover(@RequestPart("multipartFile") MultipartFile multipartFile);

    @GetMapping("/video/getTableData/selectVideoJoinCount")
    long selectVideoJoinCount(MPJLambdaWrapper<Video> videoLikeLambdaQueryWrapper);

    @GetMapping("/video/getTableData/selectCommentJoinCount")
    long selectCommentJoinCount(MPJLambdaWrapper<Comment> commentLikeLambdaQueryWrapper);

    @PostMapping("/video/getTableData/updateById")
    void updateById(Video video);

    @GetMapping("/video/getTableData/selectVideoJoinList")
    List<VideoKeywordSearchVO> selectVideoJoinList (Class<VideoKeywordSearchVO> var1, MPJLambdaWrapper<Video> wrapper);

    @GetMapping("/video/getTableData/selectVideoList")
    List<VideoData> selectList(MPJLambdaWrapper<VideoData> wrapper);
}
