package com.passport.service;

import com.common.mongo.MongoService;
import com.passport.domain.ClientUserInfo;

/**
 * 客户用户接口
 */
public interface ClientUserInfoService extends MongoService<ClientUserInfo> {

    /**
     * @param pin
     * @return
     */
    ClientUserInfo findByPin(String pin);

    /**
     * @param pin
     * @return
     */
    ClientUserInfo findByPhone(String proxyId,String pin);

    /**
     * 初始化用户密码
     *
     * @param pin
     * @param pwd
     */
    void initPass(String pin, String pwd);

    /**
     * 密码修改
     *
     * @param pin
     * @param oldPass
     * @param newPass
     * @return
     */
    void changePass(String pin, String oldPass, String newPass);
    /**
     * 密码修改
     *
     * @param pin
     * @param newPass
     * @return
     */
    void changePass(String pin, String newPass);

    /***
     * 注册
     * @param
     * @return
     */
    ClientUserInfo register(String proxyId, String pin, String pass);
//
//    /***
//     * 注册
//     * @param
//     * @return
//     */
//    ClientUserInfo regist(String upPin, String pin, String pass);


//    /**
//     * 生成登录验证码
//     * @param proxyId
//     * @param account
//     */
//    void buildLoginCode(String proxyId, String account);
//
//    /**
//     * 登录验证码验证
//     * @param proxyId
//     * @param account
//     * @param code
//     */
//    UserDTO buildLoginMobileCodeVerification(String proxyId, String account, String code);
//
//    /**
//     *
//     * @param id
//     * @param account
//     */
//    void registBuildMsg(String id, String account);
}
