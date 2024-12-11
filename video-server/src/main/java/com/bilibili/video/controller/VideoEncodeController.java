package com.bilibili.video.controller;

import com.bilibili.common.domain.api.pojo.UploadVideo;
import com.bilibili.video.service.VideoEncodeService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/videoEncode")
@Api(tags = "视频转码相关接口（不在api文档中显示）")
@Slf4j
public class VideoEncodeController {

    @Resource
    private VideoEncodeService videoEncodeService;

    /**
     * 从MinIO对象存储服务中获取视频文件，并将其作为HTTP响应的一部分返回给客户端
     * @param uploadVideo
     * @return
     */
    @ApiIgnore
    @PostMapping("/getVideoInputStream")
    public ResponseEntity<Resource> getVideoInputStream(@RequestBody UploadVideo uploadVideo){
        log.info("从Minio获取视频流");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return videoEncodeService.getVideoInputStream(uploadVideo);
    }

    /**
     * 将上传的视频文件保存到 MinIO 对象存储服务中
     * @param multipartFile
     * @throws IOException
     */
    @ApiIgnore
    @PostMapping(value = "/uploadVideo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void uploadVideo(@RequestPart("multipartFile") MultipartFile multipartFile) throws IOException {
        log.info("上传视频");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        videoEncodeService.uploadVideo(multipartFile);
    }

    /**
     * 将上传的图片文件保存到 MinIO 对象存储服务中
     * @param multipartFile
     * @throws IOException
     */
    @ApiIgnore
    @PostMapping(value = "/uploadVideoCover",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void uploadVideoCover(@RequestPart("multipartFile") MultipartFile multipartFile) throws IOException {
        log.info("上传视频封面");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        videoEncodeService.uploadVideoCover(multipartFile);
    }
}
