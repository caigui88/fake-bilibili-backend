package com.bilibili.domain.entity.video.audience_reactions;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("add_to_ensemble")
public class AddToEnsemble implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("video_id")
    private Integer videoId;

    @TableField("ensemble_id")
    private Integer ensembleId;
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}