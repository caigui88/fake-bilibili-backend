package com.bilibili.video.service.impl;

import cn.hutool.core.io.resource.InputStreamResource;
import com.bilibili.api.client.MinioApiClient;
import com.bilibili.common.domain.api.pojo.UploadVideo;
import com.bilibili.video.service.VideoEncodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

import static com.bilibili.common.constant.VideoConstant.*;

@Service
public class VideoEncodeServiceImpl implements VideoEncodeService {

    @Resource
    MinioApiClient minioApiClient;

    /**
     * 从MinIO对象存储服务中获取视频文件，并将其作为HTTP响应的一部分返回给客户端
     * @param uploadVideo
     * @return
     */
    @Override
    public ResponseEntity<Resource> getVideoInputStream(UploadVideo uploadVideo) {
        String url=uploadVideo.getUrl();
        InputStream inputStream = minioApiClient.getVideoFile(url.substring(url.lastIndexOf("/")+1));
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, HEADERS_VALUES)
                .body((Resource) resource);
    }

    /**
     * 将上传的视频文件保存到 MinIO 对象存储服务中
     * @param multipartFile
     * @throws IOException
     */
    @Override
    public void uploadVideo(MultipartFile multipartFile) throws IOException {
        minioApiClient.uploadVideoFile(multipartFile.getOriginalFilename(),multipartFile.getInputStream(),VIDEO_TYPE);
    }

    /**
     * 将上传的封面图片保存到 MinIO 对象存储服务中
     * @param multipartFile
     * @throws IOException
     */
    @Override
    public void uploadVideoCover(MultipartFile multipartFile) throws IOException {
        minioApiClient.uploadImgFile(multipartFile.getOriginalFilename(),multipartFile.getInputStream(),IMAGE_TYPE);
    }
}
