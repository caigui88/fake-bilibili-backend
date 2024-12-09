package com.bilibili.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bilibili.common.domain.search.entity.UserEntity;
import com.bilibili.common.domain.user.entity.*;
import com.bilibili.common.domain.video.entity.video_production.VideoData;
import com.bilibili.user.service.GetTableDataService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user/getTableData")
@Api(tags = "获取表格数据相关接口")
@Slf4j
public class GetTableDataController {

    @Resource
    GetTableDataService getTableDataService;

    @GetMapping("/getPrivilegeAll")
    @ApiOperation("远程调用user-server查看所有个人主页开放权限")
    public List<Privilege> getPrivilege(){
        log.info("正在远程调用user-server查看所有个人主页开放权限");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return getTableDataService.getPrivilege();
    }

    @GetMapping("/getVideoEnsembleAll")
    @ApiOperation("远程调用user-server查看视频合集")
    public List<VideoEnsemble> getVideoEnsemble(){
        log.info("正在远程调用user-server查看视频合集");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return getTableDataService.getVideoEnsemble();
    }

    @GetMapping("/getFollowAll")
    @ApiOperation("远程调用user-server查看关注表")
    public List<Follow> getFollow() {
        log.info("正在远程调用user-server查看关注表");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return getTableDataService.getFollow();
    }

    @GetMapping("/getUserAll")
    @ApiOperation("远程调用user-server查看用户表")
    public List<User> getUser() {
        log.info("正在远程调用user-server查看用户表");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return getTableDataService.getUser();
    }

    @GetMapping("/selectUserById")
    @ApiOperation("远程调用user-server根据id查询用户")
    public User selectById(Integer id) {
        log.info("正在远程调用user-server根据id查询用户");
        log.info("\n");
        log.info("\n");
        log.info("\n");
        return getTableDataService.selectById(id);
    }

    @GetMapping("/selectFollowList")
    @ApiOperation("远程调用user-server根据条件查询关注表")
    public List<Follow> selectFollowList(LambdaQueryWrapper<Follow> wrapper){
        log.info("正在远程调用user-server根据条件查询关注表");
        log.info("\n");
        return getTableDataService.selectFollowList(wrapper);
    }

    @GetMapping("/selectUserJoinList")
    @ApiOperation("远程调用user-server根据条件查询用户表")
    public List<UserEntity> selectUserJoinList(Class<UserEntity> var1, MPJLambdaWrapper<User> wrapper){
        log.info("正在远程调用user-server根据条件查询用户表");
        log.info("\n");
        return getTableDataService.selectUserJoinList(var1,wrapper);
    }

    @GetMapping("/selectUserList")
    public List<User> selectList(MPJLambdaWrapper<User> wrapper){
        log.info("正在远程调用user-server根据条件查询用户表");
        log.info("\n");
        return getTableDataService.selectList(wrapper);
    }

    @GetMapping("/getIdolCount")
    public List<IdCount> getIdolCount(List<Integer> ids){
        log.info("正在远程调用user-server的 getIdolCount 方法");
        return getTableDataService.getIdolCount(ids);
    }

    @GetMapping("/getVideoCount")
    public List<IdCount> getVideoCount(List<Integer> ids){
        log.info("正在远程调用user-server的 getVideoCount 方法");
        return getTableDataService.getVideoCount(ids);
    }


}
