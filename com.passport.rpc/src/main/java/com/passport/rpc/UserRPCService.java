package com.passport.rpc;

import com.common.rpc.StatusRpcService;
import com.common.util.RPCResult;
import com.common.util.model.GenderTypeEnum;
import com.passport.rpc.dto.UserDTO;

/**
 * 客户webservice接口
 * Created by shilun on 16-12-5.
 */
public interface UserRPCService extends StatusRpcService {

    /**
     * 注册用户
     *
     * @param proxyId
     * @param pin
     * @param pass
     * @return
     */
    public RPCResult<UserDTO> register(String proxyId, String pin, String pass);

    /**
     * 生成注册验证码
     *
     * @param proxyId
     * @param pin
     * @return
     */
    RPCResult<Boolean> buildRegistCode(String proxyId, String pin);

    /**
     * 短信注册验证
     *
     * @param proxyId
     * @param upPin
     * @param pin
     * @param code
     * @param pass
     * @return
     */
    RPCResult<UserDTO> registVerification(String proxyId, String upPin, String pin, String code, String pass);

    /**
     * 用户登录
     *
     * @param pin
     * @param pass
     * @return
     */
    RPCResult<UserDTO> login(String proxyId, String pin, String pass);

    /**
     * 用户登录
     *
     * @param token
     * @return
     */
    public RPCResult<UserDTO> loginOut(String token);

    /**
     * 根据用户pin查找用户
     *
     * @param pin
     * @return
     */
    RPCResult<UserDTO> findByPin(String pin);

    /**
     * 强制修改密码
     *
     * @param pin
     * @param pass
     * @return
     */

    RPCResult<Boolean> initPass(String pin, String pass);

    /**
     * @param token
     * @param oldPass
     * @param newPass
     * @return
     */

    RPCResult<Boolean> changePass(String token, String oldPass, String newPass);

    /**
     * 验证用户token
     *
     * @param token
     * @return
     */
    RPCResult<UserDTO> verificationToken(String token);


    /**
     * 修改电话号码
     *
     * @param pin
     * @param phone
     * @return
     */
    RPCResult<Boolean> changePhone(String pin, String phone);

    /**
     * 修改电话号码
     *
     * @param pin
     * @return
     */
    RPCResult<Boolean> changeSexType(String pin, GenderTypeEnum genderType);

    /**
     * 修改电话号码
     *
     * @param pin
     * @param nickName
     * @return
     */
    RPCResult<Boolean> changeNickName(String pin, String nickName);

    /**
     * 修改电话号码
     *
     * @param pin
     * @param sign
     * @return
     */
    RPCResult<Boolean> changeSign(String pin, String sign);

    /**
     * 生成登录验证码
     *
     * @param proxyId
     * @param pin
     * @return
     */
    RPCResult<Boolean> buildLoginCode(String proxyId, String pin);

    /**
     * 跟据登录验证码登录
     *
     * @param proxyId
     * @param pin
     * @param code
     * @return
     */
    RPCResult<UserDTO> loginCodeAndVerification(String proxyId, String pin, String code);


    /**
     * 修改生日
     *
     * @param pin
     * @param pin
     * @param birthday
     * @return
     */
    RPCResult<Boolean> changeBirthday(String pin, String birthday);


    /**
     * 忘记密码
     *
     * @param proxyId
     * @param account
     * @return
     */
    RPCResult<Boolean> forgetPass(String proxyId, String account);

    /**
     * 忘记密码验证
     * @param id
     * @param pin
     * @param code
     * @param pass
     * @return
     */
    RPCResult<Boolean> forgetPassCodeVerification(String id, String pin, String code, String pass);
}
