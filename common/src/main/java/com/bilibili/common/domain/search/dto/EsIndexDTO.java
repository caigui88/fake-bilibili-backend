package com.bilibili.common.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class EsIndexDTO {
    @JsonProperty("indexName")
    private String indexName;
    private Map<String,String> properties;
}
