package com.bilibili.video.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bilibili.api.client.MinioApiClient;
import com.bilibili.api.client.SendNoticeClient;
import com.bilibili.api.client.UserClient;
import com.bilibili.common.domain.api.file.CustomMultipartFile;
import com.bilibili.common.domain.api.pojo.UploadVideo;
import com.bilibili.common.domain.user.entity.User;
import com.bilibili.common.domain.video.dto.DeleteVideoDTO;
import com.bilibili.common.domain.video.dto.EditVideoDTO;
import com.bilibili.common.domain.video.dto.UploadPartDTO;
import com.bilibili.common.domain.video.dto.UploadVideoDTO;
import com.bilibili.common.domain.video.entity.video_production.Video;
import com.bilibili.common.domain.video.entity.video_production.VideoData;
import com.bilibili.common.domain.video.pojo.UploadPart;
import com.bilibili.video.mapper.blogger.VideoDataMapper;
import com.bilibili.video.mapper.blogger.VideoMapper;
import com.bilibili.common.util.Result;
import com.bilibili.video.service.UploadAndEditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.ScreenExtractor;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.bilibili.common.constant.VideoConstant.*;
import static com.bilibili.common.constant.VideoConstant.OPERATION_TYPE;
import static com.bilibili.common.constant.VideoConstant.OPERATION_TYPE_UPDATE;
import static com.bilibili.common.constant.VideoConstant.TABLE_ID;
import static com.bilibili.common.constant.VideoConstant.TABLE_NAME;

@Service
@Slf4j
public class UploadAndEditServiceImpl implements UploadAndEditService {

    @Resource
    VideoMapper videoMapper;

    @Resource
    UserClient userClient;

    @Resource
    VideoDataMapper videoDataMapper;

    @Resource
    MinioApiClient minioApiClient;

    @Resource
    SendNoticeClient client;

    @Resource
    RedisTemplate objectRedisTemplate;

    /**
     * 上传视频
     * @param uploadVideoDTO
     * @return
     */
    @Override
    @Transactional
    public Result<Boolean> uploadTotal(UploadVideoDTO uploadVideoDTO) {
        try {
            Video video = uploadVideoDTO.toEntity();
            LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
            String coverFile = uploadVideoDTO.toEntity().getVideoCover();
            String url = "http://localhost:9000/video/" + uploadVideoDTO.toEntity().getUrl();
            video.setUrl(url);
            if (coverFile != null && coverFile != "") {
//                hasCover=true;
                String prefixPath = "http://localhost:9000/video-cover/";
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(coverFile);
//                ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
                // 创建ImageInputStream
//                ImageInputStream iis = ImageIO.createImageInputStream(bis);
                // 读取图片文件格式
//                String imgContentType = getImageFormat(iis);
//                log.info(imgContentType);
                String imgContentType = "image/jpeg";
                String coverFileName = video.getName() + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                video.setVideoCover(prefixPath + coverFileName);
                videoMapper.insert(video);
                videoDataMapper.insert(new VideoData().setVideoId(video.getId()));
                CustomMultipartFile coverMultipartFile = new CustomMultipartFile(decodedBytes, coverFileName, imgContentType);
                queryWrapper.eq(Video::getVideoCover, UUID.randomUUID().toString().substring(0, 8) + coverFileName);

                minioApiClient.uploadImgFile(coverFileName, coverMultipartFile.getInputStream(), imgContentType);

                client.sendUploadNotice(new UploadVideo().setVideoId(video.getId()).setVideoName(video.getName()).setUrl(url).setHasCover(true));
                User user = userClient.selectById(uploadVideoDTO.toEntity().getUserId());
                client.dynamicNotice(uploadVideoDTO.toCoverDynamic(user, video));
            } else {
                videoMapper.insert(video);
                videoDataMapper.insert(new VideoData().setVideoId(video.getId()));
                client.sendUploadNotice(new UploadVideo().setVideoId(video.getId()).setVideoName(video.getName()).setUrl(url).setHasCover(false));
                User user = userClient.selectById(uploadVideoDTO.toEntity().getUserId());
                client.dynamicNotice(uploadVideoDTO.toNoCoverDynamic(user, video));
            }

            CompletableFuture<Void> sendDBChangeNotice = CompletableFuture.runAsync(() -> {
                ObjectMapper objectMapper = new ObjectMapper();
                JavaTimeModule module = new JavaTimeModule();
                // 设置LocalDateTime的序列化方式
                LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                module.addSerializer(LocalDateTime.class, localDateTimeSerializer);
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                objectMapper.registerModule(module);
                Map<String, Object> map = objectMapper.convertValue(video, Map.class);
                map.put(TABLE_NAME, VIDEO_TABLE_NAME);
                map.put(OPERATION_TYPE, OPERATION_TYPE_ADD);
                map.put(VIDEO_ID, map.get(TABLE_ID));
                map.remove(TABLE_ID);
                client.sendDBChangeNotice(map);
            });

//            CompletableFuture.allOf(uploadVideoFuture, sendNoticeFuture).join();
//            if (!uploadVideoSuccess.get()) {
//                log.error("lose");
//            } else {
//                // 所有任务成功
//                log.info("ok");
//            }
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("寄，这下真寄");
        }
    }

    /**
     * 上传分片并获取视频第一帧图片和合并后的路径
     * @param uploadPartDTO
     * @return
     * @throws IOException
     * @throws EncoderException
     */
    @Override
    public Result<List<String>> uploadPart(UploadPartDTO uploadPartDTO) throws IOException, EncoderException {
        int commaIndex = uploadPartDTO.getResumableIdentifier().indexOf(',');
        uploadPartDTO.setResumableIdentifier(uploadPartDTO.getResumableIdentifier().substring(0, commaIndex));
        String resumableIdentifier = uploadPartDTO.getResumableIdentifier();
        String videoName = "";
        String videoCover = "";
//        if(uploadPartRequest.getResumableChunkNumber()==1){
//            String path = Files.createTempDirectory(".tmp").toString();
//            File file = new File(path, "test");
//            InputStream videoFileInputStream = uploadPartRequest.getFile().getInputStream();
//            byte[] bytes=IoUtil.readBytes(videoFileInputStream);
//            ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes);
//            Files.copy(byteArrayInputStream, Paths.get(file.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
//            if (!FileTypeUtil.getType(file).equals("mp4")) {
//                file.delete();
//                return Result.error("上传恶意文件");
//            } else {
//                file.delete();
//                log.info("文件无问题");
//            }
//        }

        // 判断是否已经上传过，并生成视频封面
        if (uploadPartMap.get(resumableIdentifier) == null || uploadPartMap.get(resumableIdentifier).getHasCutImg() == false) {
            InputStream videoFileInputStream = uploadPartDTO.getFile().getInputStream();
            byte[] bytes = IoUtil.readBytes(videoFileInputStream);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            String filePath = Files.createTempDirectory(".tmp").toString();
            String coverFileName = UUID.randomUUID().toString().substring(0, 10) + ".jpg";
            String videoFileName = "video";
            File directory = new File(filePath);
            File videoFile = new File(filePath, videoFileName);
            File coverFile = new File(directory, coverFileName);
            Files.copy(byteArrayInputStream, Paths.get(videoFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            ScreenExtractor screenExtractor = new ScreenExtractor();
            MultimediaObject multimediaObject = new MultimediaObject(videoFile);
            screenExtractor.renderOneImage(multimediaObject, -1, -1, 1000, coverFile, 1);
            if (coverFile.exists()) {
                log.info("exist");
                InputStream inputStream = new FileInputStream(coverFile);
                String cover = Base64.encode(IoUtil.readBytes(inputStream));
                log.info(cover.substring(0, 20));
                videoFile.delete();
                coverFile.delete();
                UploadPart uploadPart = uploadPartMap.getOrDefault(resumableIdentifier, new UploadPart());
                uploadPart.setHasCutImg(true);
                uploadPart.setCover(cover);
                uploadPartMap.put(resumableIdentifier, uploadPart);
            }
        }

        // 生成新的文件名
        String name = resumableIdentifier + UUID.randomUUID().toString().substring(0, 10);

        minioApiClient.uploadVideoFile(name, uploadPartDTO.getFile().getInputStream(),VIDEO_TYPE);

        // 更新上传分片信息
        log.info(uploadPartMap.toString());
        Map<Integer, String> newUploadPartMap = uploadPartMap.getOrDefault(resumableIdentifier, new UploadPart()).getPartMap();
        newUploadPartMap.put(uploadPartDTO.getResumableChunkNumber(), name);
        UploadPart uploadPart = uploadPartMap.getOrDefault(resumableIdentifier, new UploadPart());
        uploadPart.setPartMap(newUploadPartMap);
        uploadPartMap.put(resumableIdentifier, uploadPart);
        uploadPartMap.get(resumableIdentifier).setTotalCount(uploadPartMap.get(resumableIdentifier).getTotalCount() + 1);

        // 合并视频分片
        if (uploadPartMap.get(resumableIdentifier).getTotalCount().equals(uploadPartDTO.getResumableTotalChunks())) {
            log.info("合并");
            videoName = resumableIdentifier + UUID.randomUUID().toString().substring(0, 10);
            videoCover = uploadPartMap.get(resumableIdentifier).getCover();

             minioApiClient.composePart(resumableIdentifier, videoName);
        }

        // 返回视频文件名和封面信息
        List<String> list = new ArrayList<>();
        list.add(videoName);
        list.add(videoCover);
        return Result.data(list);
    }

    /**
     * 编辑视频
     * @param editVideoDTO
     * @return
     */
    @Override
    public Result<Boolean> edit(EditVideoDTO editVideoDTO) {
        try {
            Map<String, Object> map = editVideoDTO.toMap();
            MultipartFile videoFile = editVideoDTO.getFile();
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            module.addSerializer(LocalDateTime.class, localDateTimeSerializer);
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            objectMapper.registerModule(module);
            map.put(TABLE_NAME, VIDEO_TABLE_NAME);
            map.put(OPERATION_TYPE, OPERATION_TYPE_UPDATE);
            Video video = editVideoDTO.toEntity();

            // 处理视频文件
            if (videoFile != null) {
                String videoUrl = UUID.randomUUID().toString().substring(0, 10) + editVideoDTO.getName();
                video.setUrl(videoUrl);
                map.put(VIDEO_URL, videoUrl);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        minioApiClient.uploadVideoFile(videoUrl, videoFile.getInputStream(), videoFile.getContentType());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                });
                CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                    while (true) {
                        String key = "encode-count";
                        Integer count = (Integer) objectRedisTemplate.opsForValue().get(key);
                        if (count < 3) {
                            client.sendUploadNotice(new UploadVideo().setVideoId(video.getId()).setUrl(videoUrl).setVideoName(video.getName()));
                            count++;
                            objectRedisTemplate.opsForValue().set(key, count);
                            break;
                        } else {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                });
            }

            // 处理封面文件
            MultipartFile coverFile = editVideoDTO.getCover();
            if (coverFile != null) {
                CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                    String coverUrl = UUID.randomUUID().toString().substring(0, 10) + coverFile.getOriginalFilename();
                    map.put(VIDEO_COVER, coverUrl);
                    try {
                        minioApiClient.uploadImgFile(coverUrl, coverFile.getInputStream(), coverFile.getContentType());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    video.setVideoCover(coverUrl);
                });
            }

            // 更新db里视频信息
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> client.sendDBChangeNotice(map));
            videoMapper.updateById(video);
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("寄");
        }
    }

    /**
     * 删除视频
     * @param deleteVideoDTO
     * @return
     */
    @Override
    public Result<Boolean> delete(DeleteVideoDTO deleteVideoDTO) {
        LambdaQueryWrapper<Video> deleteVideoWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<VideoData> deleteVideoDataWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件
        deleteVideoWrapper.eq(Video::getUserId, deleteVideoDTO.getUserId());
        deleteVideoWrapper.eq(Video::getId, deleteVideoDTO.getVideoId());
        deleteVideoDataWrapper.eq(VideoData::getVideoId, deleteVideoDTO.getVideoId());

        // 异步发送数据库变更通知
        CompletableFuture<Void> sendDBChangeNotice = CompletableFuture.runAsync(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            module.addSerializer(LocalDateTime.class, localDateTimeSerializer);
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            objectMapper.registerModule(module);
            Map<String, Object> map = new HashMap<>(3);
            map.put(TABLE_NAME, VIDEO_TABLE_NAME);
            map.put(OPERATION_TYPE, OPERATION_TYPE_DELETE);
            map.put(TABLE_ID, deleteVideoDTO.getVideoId());
            client.sendDBChangeNotice(map);
        });

        // 删除数据库记录
        videoMapper.delete(deleteVideoWrapper);
        videoDataMapper.delete(deleteVideoDataWrapper);

        return Result.success(true);
    }

    /**
     * 获取处理进度
     * @param resumableIdentifier
     * @param resumableChunkNumber
     * @return
     */
    @Override
    public ResponseEntity<Result<Boolean>> getProcessor(String resumableIdentifier, Integer resumableChunkNumber) {
        log.info("id" + resumableChunkNumber.toString());
        log.info("uploadMap" + uploadPartMap.toString());
        log.info(resumableIdentifier);
        log.info(uploadPartMap.getOrDefault(resumableIdentifier, new UploadPart()).toString());

        // 遍历上传分块映射表，判断文件是否已经上传
        for (Map.Entry<Integer, String> entry : uploadPartMap.getOrDefault(resumableIdentifier, new UploadPart()).getPartMap().entrySet()) {
            log.info("键值对" + entry.toString());
            log.info(entry.getKey().intValue() + "和" + resumableChunkNumber.intValue());
            if (entry.getKey().intValue() == (resumableChunkNumber.intValue())) {
                log.info("200");
                return ResponseEntity.ok(Result.data(true));
            }
        }

        // 返回无内容响应
        return ResponseEntity.noContent().build();
    }

    /**
     * 上确定给定图像输入流中的图像格式
     * @param iis
     * @return
     * @throws IOException
     */
    private static String getImageFormat(ImageInputStream iis) throws IOException {
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);

        if (imageReaders.hasNext()) {
            ImageReader reader = imageReaders.next();
            return reader.getFormatName();
        }

        return null;
    }
}
