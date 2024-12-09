package com.bilibili.chat.service;

import com.bilibili.common.domain.chat.dto.ChangeChatStatusDTO;
import com.bilibili.common.domain.chat.dto.ChatSessionDTO;
import com.bilibili.common.domain.chat.vo.ChatSessionVO;
import com.bilibili.common.domain.chat.vo.HistoryChatVO;
import com.bilibili.common.domain.chat.vo.PPTVO;
import com.bilibili.common.domain.chat.vo.TempSessionVO;
import com.bilibili.common.util.Result;

import java.util.List;

public interface ChatService {
//    PPTVO getPPT(String describe);

//    String getImage(String text);

    Result<TempSessionVO> createTempSession(Integer receiverId);

    Result<List<HistoryChatVO>> getHistoryChat(Integer userId, Integer receiverId);

    Result<List<ChatSessionVO>> getHistoryChatSession(Integer userId);

    Result<Boolean> changeChatStatus(ChangeChatStatusDTO changeChatStatusDTO);

    Result<Boolean> changeChatSessionTime(ChatSessionDTO chatSessionDTO);

    Result<Boolean> addChatSessionAndContent(ChatSessionDTO chatSessionDTO);
}
