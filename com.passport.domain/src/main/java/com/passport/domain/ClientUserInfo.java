package com.passport.domain;

import com.common.util.AbstractBaseEntity;
import com.common.util.AbstractSeqIdEntity;
import com.common.util.model.GenderTypeEnum;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 客户用户信息
 */
@Data
@Document(collection = "clientUserInfo")
@CompoundIndexes(
        {
                @CompoundIndex(name = "uniquePinIndex", def = "{'pin':1}", unique = true)
        })
public class ClientUserInfo extends AbstractBaseEntity implements AbstractSeqIdEntity {

    /**
     * 代理商标识
     */
    private String proxyId;

    /**
     * 用户pin(用户电话)
     */
    private String pin;
    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String head;
    /**
     * 电话
     */
    private String phone;

    /**
     * 邮件
     */
    private String email;
    /**
     * 密码
     */
    private String passwd;
    /**
     * 生日
     */
    private Date birthDay;
    /**
     * 用户状态
     */
    private Boolean status;

    /**
     * 性别
     */
    private GenderTypeEnum genderType;
    /**
     * 签名
     */
    private String sign;

}
