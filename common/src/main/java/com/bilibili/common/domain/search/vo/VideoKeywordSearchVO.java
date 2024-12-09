package com.bilibili.common.domain.search.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoKeywordSearchVO {
    Integer videoId;
    Integer authorId;
    String videoName;
    String authorName;
    String intro;
    String cover;
    LocalDateTime createTime;
    String url;
    String length;
    Integer playCount;
    Integer danmakuCount;
    Integer collectCount;
}
