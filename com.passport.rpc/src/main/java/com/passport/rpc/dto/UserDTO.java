package com.passport.rpc.dto;

import com.common.util.AbstractDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shilun on 16-12-5.
 */
@Data
public class UserDTO extends AbstractDTO implements Serializable {
    private static final long serialVersionUID = 8642175623513171274L;
    @ApiModelProperty("代理商")
    private String proxyId;
    /**
     * 用户pin(用户电话)
     */
    @ApiModelProperty("用户")
    private String pin;
    /**
     * 上级用户
     */
    @ApiModelProperty("上级用户")
    private String upPin;
    /**
     * 下级人员列表
     */
    @ApiModelProperty("下级用户")
    private List<String> pins;
    /**
     * 昵称
     */
    @ApiModelProperty("昵称")
    private String nickName;
    /**
     * 电话
     */
    @ApiModelProperty("电话")
    private String phone;
    /**
     * 性别
     */
    @ApiModelProperty("性别")
    private Integer sexType;
    /**
     * 用户状态
     */
    @ApiModelProperty("用户状态 1 启用 2停用")
    private Integer status;
    /**
     * 签名
     */
    @ApiModelProperty("签名")
    private String sign;
    /**
     * 用户token
     */
    @ApiModelProperty("用户token")
    private String token;
    /**
     * 超时时间
     */
    private Long overTime;
}
