package com.bilibili.minio.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.bilibili.minio.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.bilibili.common.constant.VideoConstant.uploadPartMap;

@Component
@Slf4j
public class MinioUtils {

    @Autowired
    private MinioConfig minioConfig;

    @Resource
    private  MinioClient minioClient;

    /**
     * 检查Bucket是否存在
     * @param bucketName
     * @return
     * @throws Exception
     */
    public boolean isBucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取Bucket策略
     * @param bucketName
     * @return
     * @throws Exception
     */
    @SneakyThrows
    public String getBucketPolicy(String bucketName) throws Exception {
        return minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取所有Bucket
     * @return
     * @throws Exception
     */
    public List<Bucket> getAllBuckets() throws Exception {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("列出所有Buckets失败", e);
            return null;
        }
    }

    /**
     * 根据Bucket名称获取Bucket相关信息
     * @param bucketName
     * @return
     * @throws Exception
     */
    @SneakyThrows(Exception.class)
    public Optional<Bucket> getBucket(String bucketName) throws Exception {
        return getAllBuckets().stream().filter(bucket -> bucket.name().equals(bucketName)).findFirst();
    }

    /**
     * 创建Bucket
     * @param bucketName
     * @throws Exception
     */
    public void createBucket(String bucketName) throws Exception  {
        try {
            if (!isBucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("创建bucketName = {}完成!", bucketName);
                return;
            }
            log.info("bucketName = {}已存在！策略为：{}", bucketName, getBucketPolicy(bucketName));
        } catch (Exception e) {
            log.error("创建bucketName = {}异常!e = {}", bucketName, e.getMessage());
        }
    }

    /**
     * 删除Bucket，true表示删除成功，false表示删除失败
     * @param bucketName
     * @return
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("删除Bucket{}失败",bucketName,e);
            return false;
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    public Boolean isObjectExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("[Minio工具类]>>>> 判断文件是否存在, 异常：", e);
            exist = false;
        }
        return exist;
    }

    /**
     * 判断文件夹是否存在
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    public boolean isFolderExist(String bucketName, String objectName) {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(objectName).recursive(false).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && objectName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            log.error("[Minio工具类]>>>> 判断文件夹是否存在，异常：", e);
            exist = false;
        }
        return exist;
    }


    /**
     *
     * @param bucketName
     * @param file
     * @return
     * @throws Exception
     */
    public String uploadFile(String bucketName, MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if(StringUtils.isBlank(originalFilename)){
            throw new RuntimeException("文件名不能为空");
        }
        // 获取文件后缀名
        String fileName = FileUtils.generateFileNameMd5(file.getName()) + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成上传到Minio的文件名
        String objectName = FileUtils.generateFileMd5(file) + fileName;

        createBucket(bucketName);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        }
        return objectName;
    }

    // 下载文件
    public InputStream downloadFile(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    // 删除文件
    public void deleteFile(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    /**
     * 合成分片
     * @param resumableIdentifier
     * @param name
     * @return
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws XmlParserException
     * @throws InternalException
     */
    public Boolean composePart(String resumableIdentifier, String name)
            throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        List<ComposeSource> composeSourceList = new ArrayList<>();
        // 获取分片信息并封装成 ComposeSource 类的集合，合并到一起
        for (Map.Entry<Integer, String> entry : uploadPartMap.get(resumableIdentifier).getPartMap().entrySet()) {
            composeSourceList.add(ComposeSource.builder().bucket("video").object(entry.getValue()).build());
        }
        minioClient.composeObject(ComposeObjectArgs.builder().sources(composeSourceList).object(name).bucket("video").build());
        return true;
    }

    /**
     * 上传视频文件
     * @param fileName
     * @param stream
     * @param contentType
     * @return
     */
    public Boolean uploadVideoFile(String fileName, InputStream stream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket("video").object(fileName)
                            .stream(stream, -1, 10485760)
                            .contentType(contentType)
                            .build());

            stream.close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    /**
     * 上传图片文件
     * @param fileName
     * @param stream
     * @param contentType
     * @return
     */
    public Boolean uploadImgFile(String fileName, InputStream stream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket("video-cover").object(fileName)
                            .stream(stream, -1, 10485760)
                            .contentType(contentType)
                            .build());

            stream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取视频文件
     * @param objectName
     * @return
     */
    public InputStream getVideoFile(String objectName) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket("video")
                    .object(objectName)
                    .build();
            return minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error("错误：" + e.getMessage());
        }
        return null;
    }


    public boolean uploadImgFile(String fileName, InputStream stream, String contentType, String bucketName) {
        bucketName = "user-cover";
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName)
                            .stream(stream, -1, 10485760)
                            .contentType(contentType)
                            .build());

            stream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
