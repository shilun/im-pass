package com.passport.domain;

import com.common.util.AbstractBaseEntity;
import com.passport.domain.model.AgentTypeEnum;
import com.passport.domain.model.VersionTypeEnum;
import lombok.Data;

/**
 * 
 * @desc 软件包 soft_ware
 *
 */
@Data
public class SoftWare extends AbstractBaseEntity implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 名称
     */
    private String name;
    /**
     * 代理商
     */
    private String proxyId;
    /**
     * 版本说明
     */
    private String versionDescribes;
    /**
     * 软件类型 1 android 2 iso
     */
    private AgentTypeEnum osType;
    /**
     * 下载地址
     */
    private String url;
    /**
     * 版本
     */
    private String version;
    /**
     * 版本类型 1增量 2全量
     */
    private VersionTypeEnum versionType;

    /**
     * 状态
     */
    private Boolean status;
    /**
     * 是否强制更新 1：是  2：否
     */
    private Integer mandatory;

    /***
     * app标识 用于跟新app版本，由app上报
     */
    private String appSign;

}
