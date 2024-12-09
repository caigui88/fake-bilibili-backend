package com.bilibili.chat.controller;

import com.bilibili.chat.service.ChatService;
import com.bilibili.common.domain.chat.dto.ChangeChatStatusDTO;
import com.bilibili.common.domain.chat.dto.ChatSessionDTO;
import com.bilibili.common.domain.chat.vo.ChatSessionVO;
import com.bilibili.common.domain.chat.vo.HistoryChatVO;
import com.bilibili.common.domain.chat.vo.PPTVO;
import com.bilibili.common.domain.chat.vo.TempSessionVO;
import com.bilibili.common.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/chat")
@Api(tags = "聊天相关接口")
@Slf4j
public class ChatController {

    @Resource
    ChatService chatService;

//    @GetMapping("/getPPT/{describe}")
//    @ApiOperation("获取PPT")
//    public PPTVO getPPT(@PathVariable String describe) throws IOException, InterruptedException {
//        log.info("正在获取PPT");
//        log.info("\n");
//        log.info("\n");
//        log.info("\n");
//        return chatService.getPPT(describe);
//    }

//    @GetMapping("/getImage/{text}")
//    @ApiOperation("获取图片")
//    public String getImage(@PathVariable String text) throws Exception {
//        log.info("正在获取图片");
//        log.info("\n");
//        log.info("\n");
//        log.info("\n");
//        return chatService.getImage(text);
//    }

    @GetMapping("/createTempSession/{receiverId}")
    @ApiOperation("创建临时会话")
    public Result<TempSessionVO> createTempSession(@PathVariable Integer receiverId){
        log.info("正在创建临时会话");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return chatService.createTempSession(receiverId);
    }

    @GetMapping("/getHistoryChat/{userId}/{receiverId}")
    @ApiOperation("获取历史聊天记录")
    public Result<List<HistoryChatVO>> getHistoryChat(@PathVariable Integer userId, @PathVariable Integer receiverId){
        log.info("正在获取历史聊天记录");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return chatService.getHistoryChat(userId,receiverId);
    }

    @GetMapping("/getHistoryChatSession/{userId}")
    @ApiOperation("获取历史聊天会话列表")
    public Result<List<ChatSessionVO>> getHistoryChatSession(@PathVariable Integer userId){
        log.info("正在获取历史聊天会话列表");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return chatService.getHistoryChatSession(userId);
    }

    @PostMapping("/changeChatStatus")
    @ApiOperation("修改聊天记录的状态从未读到已读")
    public Result<Boolean> changeChatStatus(@RequestBody ChangeChatStatusDTO changeChatStatusDTO){
        log.info("正在修改聊天记录的状态：从未读到已读");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return chatService.changeChatStatus(changeChatStatusDTO);
    }

    @PostMapping("/changeChatSessionTime")
    @ApiOperation("修改聊天会话的最后聊天时间和最后聊天内容")
    public Result<Boolean> changeChatSessionTime(@RequestBody ChatSessionDTO chatSessionDTO){
        log.info("正在修改聊天会话的最后聊天时间和最后聊天内容");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return chatService.changeChatSessionTime(chatSessionDTO);
    }

    @PostMapping("/addChatSessionAndContent")
    @ApiOperation("新增聊天会话和聊天内容")
    public Result<Boolean> addChatSessionAndContent(@RequestBody ChatSessionDTO chatSessionDTO){
        log.info("正在新增聊天会话和聊天内容");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return chatService.addChatSessionAndContent(chatSessionDTO);
    }

}
