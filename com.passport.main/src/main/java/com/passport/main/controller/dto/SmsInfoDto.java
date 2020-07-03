package com.passport.main.controller.dto;

import com.common.util.AbstractDTO;
import lombok.Data;

/**
 * 电话内容
 */
@Data
public class SmsInfoDto extends AbstractDTO {
    /**
     * 代理商id
     */
    private String proxyId;
    /**
     * 电话号码
     */
    private String phone;

    /**
     * 电话内容
     */
    private String contnet;

}
