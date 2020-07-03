package com.passport.api.controller.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Luo
 * @date 2018/8/31 14:06
 */
@Data
public class RegisterVerDto extends RegisterDto {
    private static final long serialVersionUID = 1688043110049698697L;
    @ApiModelProperty(value = "验证码")
    private String code;
    @ApiModelProperty(value = "密码")
    private String pass;
    @ApiModelProperty(value = "推荐用户")
    private String upPin;
}
