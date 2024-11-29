package com.bilibili.mapper.chat;

import com.bilibili.domain.entity.chat.ChatSession;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends MPJBaseMapper<ChatSession> {
}
