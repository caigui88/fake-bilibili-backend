package com.bilibili.minio.service.impl;

import com.bilibili.common.util.Result;
import com.bilibili.minio.service.MinioService;
import com.bilibili.minio.util.MinioUtils;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioServiceImpl implements MinioService {


    @Autowired
    MinioUtils minioUtils;

    /**
     *
     * @param bucketName 存储桶名称
     * @param file MultipartFile 类型的文件对象
     * @throws Exception
     */
    @Override
    public Result uploadFile(String bucketName, MultipartFile file) throws Exception {
        // 上传文件到 MinIO 服务器
        minioUtils.uploadFile(bucketName, file);
        return Result.success();
    }

    @Override
    public void composePart(String resumableIdentifier, String name)
            throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        minioUtils.composePart(resumableIdentifier, name);
    }

    @Override
    public void uploadVideoFile(String videoUrl, InputStream inputStream, String contentType) {
        minioUtils.uploadVideoFile(videoUrl, inputStream, contentType);
    }

    @Override
    public InputStream downloadVideoFile(String fileName) {
        InputStream inputStream = minioUtils.getVideoFile(fileName);
        if(inputStream == null){
            throw new RuntimeException("文件不存在");
        }
        return inputStream;
    }

    @Override
    public void uploadImageFile(String coverUrl, InputStream inputStream, String contentType) {
        minioUtils.uploadImgFile(coverUrl, inputStream, contentType);
    }

    @Override
    public void uploadImageFile(String coverName, InputStream inputStream, String contentType, String bucketName) {
        minioUtils.uploadImgFile(coverName, inputStream, contentType, bucketName);
    }


}
