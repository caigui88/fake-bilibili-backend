package com.bilibili.common.domain.user.dto;

import com.bilibili.common.domain.video.entity.audience_reactions.AddToEnsemble;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class AddToEnsembleDTO {
    @ApiModelProperty("视频id")
    private Integer videoId;
    @ApiModelProperty("合集id")
    private Integer ensembleId;
    public AddToEnsemble toEntity(){
        AddToEnsemble addToEnsemble=new AddToEnsemble();
        BeanUtils.copyProperties(this,addToEnsemble);
        return addToEnsemble;
    }
}