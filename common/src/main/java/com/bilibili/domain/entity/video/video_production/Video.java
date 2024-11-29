package com.bilibili.domain.entity.video.video_production;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("video")
@Accessors(chain = true)
public class Video {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("user_id")
    private Integer userId;
    @TableField("intro")
    private String intro;

    @TableField("length")
    private String length;
    @TableField("name")
    private String name;
    @TableField("url")
    private String url;
    @TableField("cover")
    private String cover;
}
