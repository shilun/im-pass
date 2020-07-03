package com.passport.domain;

import com.common.util.AbstractBaseEntity;
import lombok.Data;

/**
 *
 * @desc 短信内容
 *
 */
@Data
public class SMSInfo extends AbstractBaseEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 签名
	 */
	private String sign;
	/**
	 * 短信云营商appid
	 */
	private String appId;
	/**
	 * 调用业务系统
	 */
	private String sender;
	/**
	 * 手机
	 */
	private String mobile;
	/**
	 * 内容
	 */
	private String content;
	/**
	 * 发送次数
	 */
	private Integer executeCount;
	/**
	 * 最小发送次数
	 */
	private Integer minExecuteCount;

	/**
	 * 状态 1 发送成功 2 发送失败
	 */
	private Boolean status;
}
