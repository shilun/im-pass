package com.passport.rpc.dto;

import com.common.util.AbstractDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ProxyDto extends AbstractDTO {
    /**
     * 公司名称
     */
    private String name;
    /**
     * 结整时间
     */
    private Date endTime;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 联系人
     */
    private String linkMan;
    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     */
    private Boolean status;
    /**
     * 自定义注册ui
     *
     */
    private Boolean selfReg;
    /**
     * 域名
     */
    private String[] domain;

    /**
     * 彩票费率
     */
    private BigDecimal cpRate;
    /**
     * 棋牌费率
     */
    private BigDecimal qpRate;
    /**
     * 服务费
     */
    private BigDecimal serviceMoney;
}
