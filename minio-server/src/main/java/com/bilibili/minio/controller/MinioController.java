package com.bilibili.minio.controller;

import com.bilibili.common.util.Result;
import com.bilibili.minio.service.MinioService;
import com.bilibili.minio.util.MinioUtils;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    MinioService minioService;

    @PostMapping("/upload")
    public Result upload(@RequestParam("bucketName") String bucketName ,@RequestParam("file") MultipartFile file) throws Exception {
        return minioService.uploadFile(bucketName,file);
    }

    @PostMapping("/compose")
    public void composePart(@RequestParam String resumableIdentifier,
                                      @RequestParam String name)
            throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        minioService.composePart(resumableIdentifier,name);
    }

    @PostMapping("/upload/video")
    public void uploadVideoFile(@RequestParam String videoUrl,
                                  @RequestParam InputStream inputStream,
                                  @RequestParam String contentType) throws IOException {
        minioService.uploadVideoFile(videoUrl,inputStream,contentType);
    }

    @GetMapping("/download/video")
    public InputStream getVideoFile(@RequestParam String fileName){
        return minioService.downloadVideoFile(fileName);
    }

    @PostMapping("/upload/image")
    public void uploadImgFile(@RequestParam String coverUrl,
                                @RequestParam InputStream inputStream,
                                String contentType) throws IOException {
        minioService.uploadImageFile(coverUrl,inputStream,contentType);
    }

    @PostMapping("/minio/upload/imageWithBucket")
    void uploadImgFile(String coverName, InputStream inputStream, String contentType, String bucketName){
        minioService.uploadImageFile(coverName, inputStream, contentType, bucketName);
    }


}
