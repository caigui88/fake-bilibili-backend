package com.bilibili.api.client;

import com.bilibili.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@FeignClient("minio-server")
public interface MinioApiClient {

    /**
     * 上传文件
     * @param bucketName
     * @param file
     * @return
     */
    @PostMapping("/minio/upload")
    Result upload(@RequestParam("bucketName") String bucketName , @RequestParam("file") MultipartFile file);

    /**
     * 合成视频
     * @param resumableIdentifier
     * @param videoName
     */
    @PostMapping("/minio/compose")
    void composePart(String resumableIdentifier, String videoName);

    /**
     * 上传视频
     * @param videoUrl
     * @param inputStream
     * @param contentType
     */
    @PostMapping("/minio/upload/video")
    void uploadVideoFile(String videoUrl, InputStream inputStream, String contentType);

    /**
     * 获取视频文件
     * @param substring
     * @return
     */
    @GetMapping("/minio/download/video")
    InputStream getVideoFile(String substring);

    /**
     * 上传图片文件
     * @param coverUrl
     * @param inputStream
     * @param contentType
     */
    @PostMapping("/minio/upload/image")
    void uploadImgFile(String coverUrl, InputStream inputStream, String contentType);

    @PostMapping("/minio/upload/imageWithBucket")
    void uploadImgFile(String coverName, InputStream inputStream, String contentType, String bucketName);
}
