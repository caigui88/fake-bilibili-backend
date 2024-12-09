package com.bilibili.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bilibili.api.client.UserClient;
import com.bilibili.chat.mapper.ChatMapper;
import com.bilibili.chat.mapper.ChatServiceMapper;
import com.bilibili.chat.mapper.ChatSessionMapper;
import com.bilibili.chat.service.ChatService;
import com.bilibili.common.domain.chat.dto.ChangeChatStatusDTO;
import com.bilibili.common.domain.chat.dto.ChatSessionDTO;
import com.bilibili.common.domain.chat.entity.Chat;
import com.bilibili.common.domain.chat.entity.ChatSession;
import com.bilibili.common.domain.chat.entity.NoticeCount;
import com.bilibili.common.domain.chat.vo.ChatSessionVO;
import com.bilibili.common.domain.chat.vo.HistoryChatVO;
import com.bilibili.common.domain.chat.vo.PPTVO;
import com.bilibili.common.domain.chat.vo.TempSessionVO;
import com.bilibili.common.domain.user.entity.User;
import com.bilibili.common.util.Result;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChatServiceImpl implements ChatService {

    @Resource
    ChatSessionMapper chatSessionMapper;

    @Resource
    ChatMapper chatMapper;

    @Resource
    ChatServiceMapper chatServiceMapper;

    @Resource
    UserClient userClient;


//    @Override
//    public PPTVO getPPT(String describe) {
//        return null;
//    }

//    @Override
//    public String getImage(String text) {
//        return "";
//    }

    /**
     * 创建临时会话
     * @param receiverId
     * @return
     */
    @Override
    public Result<TempSessionVO> createTempSession(Integer receiverId) {
        User u = userClient.selectById(receiverId);
        return Result.data(new TempSessionVO().setCover(u.getCover()).setNickName(u.getNickname()));
    }

    /**
     * 获取历史聊天记录内容
     * @param userId
     * @param receiverId
     * @return
     */
    @Override
    public Result<List<HistoryChatVO>> getHistoryChat(Integer userId, Integer receiverId) {
        LambdaQueryWrapper<Chat> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Chat::getSenderId, userId);
        wrapper1.eq(Chat::getReceiverId, receiverId);
        LambdaQueryWrapper<Chat> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Chat::getReceiverId, userId);
        wrapper2.eq(Chat::getSenderId, receiverId);
        List<Chat> list1 = chatMapper.selectList(wrapper1);
        List<Chat> list2 = chatMapper.selectList(wrapper2);
        list1.addAll(list2);
        List<HistoryChatVO> VOs = new ArrayList<>();
        for (Chat chat : list1) {
            VOs.add(new HistoryChatVO(chat));
        }
        VOs = VOs.stream()
                .sorted(Comparator.comparing(HistoryChatVO::getCreateTime).reversed())
                .collect(Collectors.toList());
        return Result.data(VOs);
    }

    /**
     * 获取历史会话列表
     * @param userId
     * @return
     */
    @Override
    public Result<List<ChatSessionVO>> getHistoryChatSession(Integer userId) {
        List<ChatSessionVO> selfVOs= chatServiceMapper.getSelfSession(userId);
        List<ChatSessionVO> otherVOs= chatServiceMapper.getOtherSession(userId);
        selfVOs.addAll(otherVOs);
        List<Integer> idList=new ArrayList<>();
        //获取会话集合的id集合
        for(ChatSessionVO sessionVO : selfVOs){
            idList.add(sessionVO.getSessionId());
        }
        if(idList.size()>0){
            //获取每个会话的未读消息数和设置每个会话的未读状态
            List<NoticeCount> noticeCounts= chatServiceMapper.getNoticeCounts(idList,userId);
            for(ChatSessionVO sessionVO : selfVOs){
                for(NoticeCount noticeCount : noticeCounts){
                    if(noticeCount.getSessionId().equals(sessionVO.getSessionId())){
                        if(noticeCount.getNoticeCount()>0){
                            sessionVO.setCount(noticeCount.getNoticeCount());
                            sessionVO.setStatus(false);
                            break;
                        }
                    }
                }
            }
        }
        //按创建时间倒序排
        if(selfVOs.size()>0){
            selfVOs = selfVOs.stream()
                    .sorted(Comparator.comparing(ChatSessionVO::getUpdateTime).reversed())
                    .collect(Collectors.toList());
        }
        return Result.data(selfVOs);
    }

    /**
     * 更新接收者的聊天状态
     * @param changeChatStatusDTO
     * @return
     */
    @Override
    public Result<Boolean> changeChatStatus(ChangeChatStatusDTO changeChatStatusDTO) {
        LambdaUpdateWrapper<Chat> wrapper = new LambdaUpdateWrapper<>();

        // 设置更新条件
        wrapper.set(Chat::getStatus, 1);
        wrapper.eq(Chat::getSenderId, changeChatStatusDTO.getReceiverId());
        wrapper.eq(Chat::getReceiverId, changeChatStatusDTO.getUserId());

        // 执行更新操作
        chatMapper.update(null, wrapper);
        return Result.success(true);
    }

    /**
     * 更新会话时间和内容
     * @param chatSessionDTO
     * @return
     */
    @Override
    public Result<Boolean> changeChatSessionTime(ChatSessionDTO chatSessionDTO) {
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatSession::getSenderId, chatSessionDTO.getSenderId());
        wrapper.eq(ChatSession::getReceiverId, chatSessionDTO.getReceiverId());
        wrapper.set(ChatSession::getUpdateTime, LocalDateTime.now());
        wrapper.set(ChatSession::getUpdateContent, chatSessionDTO.getUpdateContent());
        chatSessionMapper.update(null, wrapper);
        return Result.success(true);
    }

    /**
     * 添加聊天记录
     * @param chatSessionDTO
     * @return
     */
    @Override
    public Result<Boolean> addChatSessionAndContent(ChatSessionDTO chatSessionDTO) {
        // 查询之前是否存在自己向他人或他人向自己发起的会话
        // wrapper1和wrapper2，分别用于查询是否存在从发送者到接收者或从接收者到发送者的会话。
        LambdaQueryWrapper<ChatSession> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(ChatSession::getSenderId, chatSessionDTO.getSenderId());
        wrapper1.eq(ChatSession::getReceiverId, chatSessionDTO.getReceiverId());
        LambdaQueryWrapper<ChatSession> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(ChatSession::getSenderId, chatSessionDTO.getReceiverId());
        wrapper2.eq(ChatSession::getReceiverId, chatSessionDTO.getSenderId());

        ChatSession c1=chatSessionMapper.selectOne(wrapper1);
        ChatSession c2=chatSessionMapper.selectOne(wrapper2);

        ChatSession chatSession=chatSessionDTO.toSessionEntity();
        chatSession.setUpdateTime(LocalDateTime.now());
        //如果查询过后发现该会话之前并未存在
        if ( c1== null && c2 == null) {
            chatSessionMapper.insert(chatSession);
        }else {
            chatServiceMapper.updateChatSession(chatSessionDTO.getUpdateContent(), LocalDateTime.now(),chatSessionDTO.getSenderId(),chatSessionDTO.getReceiverId());
        }
        chatMapper.insert(chatSessionDTO.toChatEntity().setContent(chatSession.getUpdateContent()));
        return Result.success(true);
    }
}
