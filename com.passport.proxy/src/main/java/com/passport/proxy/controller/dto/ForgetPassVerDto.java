package com.passport.proxy.controller.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Luo
 * @date 2018/8/31 18:05
 */
@Data
public class ForgetPassVerDto {
    private static final long serialVersionUID = 885207316961913603L;
    /**
     * 账户
     */
    private String account;
    @ApiModelProperty(value = "验证码")
    private String code;
    @ApiModelProperty(value = "新密码")
    private String pass;
}
