package com.bilibili.common.domain.search.entity;

import lombok.Data;

@Data
public class UserEntity {
    private String nickname;
    private Integer id;
    private String intro;
    private String cover;
}
