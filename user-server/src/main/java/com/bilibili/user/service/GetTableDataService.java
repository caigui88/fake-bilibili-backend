package com.bilibili.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bilibili.common.domain.search.entity.UserEntity;
import com.bilibili.common.domain.user.entity.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.List;

public interface GetTableDataService {
    List<Privilege> getPrivilege();

    List<VideoEnsemble> getVideoEnsemble();

    List<Follow> getFollow();

    List<User> getUser();

    User selectById(Integer id);

    List<Follow> selectFollowList(LambdaQueryWrapper<Follow> wrapper);

    List<UserEntity> selectUserJoinList(Class<UserEntity> var1, MPJLambdaWrapper<User> wrapper);

    List<User> selectList(MPJLambdaWrapper<User> wrapper);

    List<IdCount> getIdolCount(List<Integer> ids);

    List<IdCount> getVideoCount(List<Integer> ids);
}
