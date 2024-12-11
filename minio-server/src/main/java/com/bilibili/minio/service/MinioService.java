package com.bilibili.minio.service;

import com.bilibili.common.util.Result;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public interface MinioService {

    /**
     * 上传文件到 MinIO 服务器。
     *
     * @param bucketName 存储桶名称
     * @param file MultipartFile 类型的文件对象
     * @throws Exception 上传文件异常
     */
    public Result uploadFile(String bucketName, MultipartFile file) throws Exception;

    void composePart(String resumableIdentifier, String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    void uploadVideoFile(String videoUrl, InputStream inputStream, String contentType);

    InputStream downloadVideoFile(String fileName);

    void uploadImageFile(String coverUrl, InputStream inputStream, String contentType);

    void uploadImageFile(String coverName, InputStream inputStream, String contentType, String bucketName);
}
