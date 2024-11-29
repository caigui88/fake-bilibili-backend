package com.bilibili.mapper.user;

import com.bilibili.domain.entity.user.Follow;
import com.bilibili.domain.entity.user.IdCount;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowMapper extends MPJBaseMapper<Follow> {
    @Select({
            "<script>",
            "SELECT idol_id AS id, COUNT(*) AS count",
            "FROM follow",
            "WHERE idol_id IN",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "GROUP BY idol_id",
            "</script>"
    })
    List<IdCount> getFansCount(List<Integer> ids);

    @Select({
            "<script>",
            "SELECT fans_id AS id, COUNT(*) AS count",
            "FROM follow",
            "WHERE fans_id IN",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "GROUP BY fans_id",
            "</script>"
    })
    List<IdCount> getIdolCount(List<Integer> ids);

    @Select({
            "<script>",
            "select user_id as id,count(*) as count",
            "from video ",
            "where user_id in",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "GROUP BY user_id",
            "</script>"
    })
    List<IdCount> getVideoCount(List<Integer> ids);
}
