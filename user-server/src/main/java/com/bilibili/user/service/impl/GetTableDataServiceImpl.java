package com.bilibili.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bilibili.common.domain.search.entity.UserEntity;
import com.bilibili.common.domain.user.entity.*;
import com.bilibili.user.mapper.FollowMapper;
import com.bilibili.user.mapper.PrivilegeMapper;
import com.bilibili.user.mapper.UserMapper;
import com.bilibili.user.mapper.VideoEnsembleMapper;
import com.bilibili.user.service.GetTableDataService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import javax.annotation.Resource;
import java.util.List;

public class GetTableDataServiceImpl implements GetTableDataService {

    @Resource
    PrivilegeMapper privilegeMapper;

    @Resource
    VideoEnsembleMapper videoEnsembleMapper;

    @Resource
    FollowMapper followMapper;

    @Resource
    UserMapper userMapper;

    @Override
    public List<Privilege> getPrivilege() {
        return privilegeMapper.selectList(null);
    }

    @Override
    public List<VideoEnsemble> getVideoEnsemble() {
        return videoEnsembleMapper.selectList(null);
    }

    @Override
    public List<Follow> getFollow() {
        return followMapper.selectList(null);
    }

    @Override
    public List<User> getUser() {
        return userMapper.selectList(null);
    }

    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<Follow> selectFollowList(LambdaQueryWrapper<Follow> wrapper) {
        return followMapper.selectList(wrapper);
    }

    @Override
    public List<UserEntity> selectUserJoinList(Class<UserEntity> var1, MPJLambdaWrapper<User> wrapper) {
        return userMapper.selectJoinList(UserEntity.class, wrapper);
    }

    @Override
    public List<User> selectList(MPJLambdaWrapper<User> wrapper) {
        return userMapper.selectList(wrapper);
    }

    @Override
    public List<IdCount> getIdolCount(List<Integer> ids) {
        return followMapper.getIdolCount(ids);
    }

    @Override
    public List<IdCount> getVideoCount(List<Integer> ids) {
        return followMapper.getVideoCount(ids);
    }
}
