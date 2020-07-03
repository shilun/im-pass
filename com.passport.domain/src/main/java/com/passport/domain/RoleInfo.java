package com.passport.domain;

import com.common.util.AbstractBaseEntity;
import lombok.Data;

/**
 * 管理员角色信息
 */
@Data
public class RoleInfo extends AbstractBaseEntity {
    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 资源名称
     */
    private String resources;
}
