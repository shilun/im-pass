package com.passport.api.controller.dto;

import com.common.util.model.GenderTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Luo
 * @date 2018/8/31 18:00
 */
@Data
public class ChangeSexDto implements Serializable {
    private static final long serialVersionUID = -1133807397912730601L;
    @ApiModelProperty(value = "新性别")
    private GenderTypeEnum genderType;
}
