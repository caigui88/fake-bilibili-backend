package com.bilibili.api.client;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bilibili.common.domain.search.entity.UserEntity;
import com.bilibili.common.domain.user.entity.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "user-server")
public interface UserClient {

    @GetMapping("/user/getTableData/getPrivilegeAll")
    List<Privilege> getPrivilege();

    @GetMapping("/user/getTableData/getVideoEnsembleAll")
    List<VideoEnsemble> getVideoEnsemble();

    @GetMapping("/user/getTableData/getFollowAll")
    List<Follow> getFollow();

    @GetMapping("/user/getTableData/getUserAll")
    List<User> getUser();

    @GetMapping("/user/getTableData/selectUserById")
    User selectById(Integer id);

    @GetMapping("/user/getTableData/selectFollowList")
    List<Follow> selectFollowList(LambdaQueryWrapper<Follow> wrapper);

    @GetMapping("/user/getTableData/selectUserJoinList")
    List<UserEntity> selectJoinList(Class<UserEntity> var1, MPJLambdaWrapper<User> wrapper);

    @GetMapping("/user/getTableData/selectUserList")
    List<User> selectList(MPJLambdaWrapper<User> wrapper);

    @GetMapping("/user/getTableData/getIdolCount")
    List<IdCount> getIdolCount(List<Integer> ids);

    @GetMapping("/user/getTableData/getVideoCount")
    List<IdCount> getVideoCount(List<Integer> ids);
}
