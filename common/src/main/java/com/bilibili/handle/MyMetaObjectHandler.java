package com.bilibili.handle;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *  实现了MetaObjectHandler接口，用于在 MyBatis-Plus中自动填充实体类的字段。
 *  具体来说，它在插入和更新操作时自动填充 `createTime` 和 `updateTime` 字段。
 *
 * insertFill 方法在插入操作时调用，自动填充 createTime 字段为当前时间。
 * updateFill 方法在更新操作时调用，自动填充 updateTime 字段为当前时间。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(org.apache.ibatis.reflection.MetaObject metaObject) {
        // 设置默认值

        // 新用户的昵称为 "新用户" + 随机字符串
        this.setFieldValByName("nickname","新用户"+ UUID.randomUUID().toString().substring(0,10),metaObject);
        // 新用户个人页面的背景图片为默认图片
        this.setFieldValByName("cover","https://xx.png",metaObject);
        // 新用户创建时间为当前时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 弹幕数量默认为0
        this.setFieldValByName("danmakuCount",0,metaObject);
        // 视频播放数量默认为0
        this.setFieldValByName("playCount",0,metaObject);
        // 点赞数量默认为0
        this.setFieldValByName("likeCount",0,metaObject);
        // 收藏数量默认为0
        this.setFieldValByName("collectCount",0,metaObject);
        // 评论数量默认为0
        this.setFieldValByName("commentCount",0,metaObject);
        // 消息是否已读
        this.setFieldValByName("status",0,metaObject);
        // 收藏夹数量默认为0
        this.setFieldValByName("collectGroup",0,metaObject);
        // 是否最近点赞默认为false
        this.setFieldValByName("recentlyLiked",0,metaObject);
        // 粉丝数量默认为0
        this.setFieldValByName("fansCount",0,metaObject);
        // 关注数量默认为0
        this.setFieldValByName("idolList",0,metaObject);
    }

    @Override
    public void updateFill(org.apache.ibatis.reflection.MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
