package com.bilibili.domain.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("fans_id")
    Integer fansId;
    @TableField("idol_id")
    Integer idolId;
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    LocalDateTime createTime;
}