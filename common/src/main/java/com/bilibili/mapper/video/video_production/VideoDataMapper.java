package com.bilibili.mapper.video.video_production;

import com.bilibili.domain.entity.video.video_production.VideoData;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoDataMapper extends MPJBaseMapper<VideoData> {

    // 构造函数、getters和setters可以由@Data注解自动生成
}
