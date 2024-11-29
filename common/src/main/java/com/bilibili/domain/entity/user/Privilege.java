package com.bilibili.domain.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@TableName("privilege")
@Accessors(chain = true)
public class Privilege {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("user_id")
    Integer userId;
    @TableField(value = "collect_group",fill = FieldFill.INSERT)
    Integer collectGroup;
    @TableField(value = "recently_like",fill = FieldFill.INSERT)
    Integer recentlyLike;
    @TableField(value = "fans_list",fill = FieldFill.INSERT)
    Integer fansList;
    @TableField(value = "idol_list",fill = FieldFill.INSERT)
    Integer idolList;
}
