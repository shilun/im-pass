package com.passport.service.rpc;

import com.common.exception.BizException;
import com.common.rpc.StatusRpcServiceImpl;
import com.common.security.DesDecrypter;
import com.common.security.DesEncrypter;
import com.common.security.MD5;
import com.common.util.BeanCoper;
import com.common.util.DateUtil;
import com.common.util.RPCResult;
import com.common.util.StringUtils;
import com.common.util.model.GenderTypeEnum;
import com.passport.domain.ClientUserInfo;
import com.passport.rpc.SMSInfoRPCService;
import com.passport.rpc.UserRPCService;
import com.passport.rpc.dto.UserDTO;
import com.passport.service.ClientUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@org.apache.dubbo.config.annotation.Service
public class UserRPCServiceImpl extends StatusRpcServiceImpl implements UserRPCService {

    /**
     * 登录验证码
     */
    public static final String login_build_code = "passport.login.code.{0}.{1}";
    /**
     * 注册验证码
     */
    public static final String regist_build_code = "passport.regist.code.{0}.{1}";
    /**
     * 忘记密码验证码
     */
    private final String forget_pass = "passport.userrpc.forgetpass.{0}.{1}";
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ClientUserInfoService clientUserInfoService;

    private final String LOGIN_TOKEN = "passport.login.token.{0}";
    private final String LOGIN_PIN = "passport.login.{1}";

    @Value("${app.token.encode.key}")
    private String appTokenEncodeKey;
    @Value("${app.passKey}")
    private String passKey;

    @Resource
    private SMSInfoRPCService smsInfoRPCService;

    @Override
    public RPCResult<UserDTO> register(String proxyId, String pin, String pass) {
        RPCResult<UserDTO> result = new RPCResult<>();
        try {
            ClientUserInfo info = clientUserInfoService.register(proxyId, pin, pass);
            UserDTO dto = BeanCoper.copyProperties(UserDTO.class, info);
            result.setData(dto);
            result.setSuccess(true);
        } catch (DuplicateKeyException e) {
            log.error("client.regist.duplicate.error", e);
            result.setCode("client.regist.duplicate.error");
            result.setMessage("注册失败,数据重复");
        } catch (Exception e) {
            log.error("client.regist.error", e);
            result.setCode("client.regist.error");
            result.setMessage("注册失败");
        }
        return result;
    }

    @Override
    public RPCResult<UserDTO> login(String proxyId, String pin, String pass) {
        RPCResult<UserDTO> result = new RPCResult<>();
        try {
            if (StringUtils.isBlank(pass)) {
                throw new BizException("pass.empty", "密码不能为空");
            }
            if (StringUtils.isBlank(pin)) {
                throw new BizException("account.empty", "账户不能为空");
            }
            ClientUserInfo userInfo = null;
            if (StringUtils.isMobileNO(pin)) {
                userInfo = clientUserInfoService.findByPhone(proxyId, pin);
            } else {
                clientUserInfoService.findByPin(pin);
            }

            if (userInfo == null || userInfo.getStatus()==false) {
                throw new BizException("login.error", "账户异常");
            }
            if (!userInfo.getProxyId().equals(proxyId)) {
                throw new BizException("account.error", "账户异常，账户数据错误，请联系运营商");
            }
            pass = MD5.MD5Str(pass, passKey);
            if (!pass.equals(userInfo.getPasswd())) {
                throw new BizException("密码错误");
            }
            UserDTO userDTO = buildToken(userInfo);
            result.setData(userDTO);
            result.setSuccess(true);
            return result;
        } catch (BizException e) {
            result.setException(e);
        } catch (Exception e) {
            log.error("userrpc.login.error.%s.%s", new Object[]{pin, pass});
            result.setCode("login.error");
        }
        return result;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public RPCResult<UserDTO> loginOut(String token) {
        RPCResult<UserDTO> result = new RPCResult<>();
        try {
            String key = MessageFormat.format(LOGIN_TOKEN, token);
            UserDTO o = (UserDTO) redisTemplate.opsForValue().get(key);
            redisTemplate.delete(MessageFormat.format(LOGIN_PIN, o.getPin()));
            redisTemplate.delete(key);
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            log.error("登出失败", e);
        }
        result.setSuccess(false);
        result.setCode("passport.login.out.error");
        result.setMessage("登出失败");
        return result;
    }

    @Override
    public RPCResult<UserDTO> findByPin(String pin) {
        RPCResult<UserDTO> result = new RPCResult<>();
        try {
            ClientUserInfo userInfo = clientUserInfoService.findByPin(pin);
            UserDTO userDTO = BeanCoper.copyProperties(UserDTO.class, userInfo);
            result.setData(userDTO);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("userrpc.findByPin.error.%s", pin, e);
            result.setCode("userrpc.findByPin.error");
            result.setMessage("查询用户pin失败");
        }
        return result;
    }


    @Override
    public RPCResult<Boolean> initPass(String pin, String pass) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            String loginPinKey = MessageFormat.format(LOGIN_PIN, pin);
            String token = (String) redisTemplate.opsForValue().get(loginPinKey);
            String tokenKey = MessageFormat.format(LOGIN_TOKEN, token);
            redisTemplate.delete(loginPinKey);
            redisTemplate.delete(tokenKey);
            clientUserInfoService.initPass(pin, pass);
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setCode("userrpc.changePass.error");
            result.setMessage("修改用户密码失败");
            log.error("userrpc.changePass.error.%s.%s", new Object[]{pin, pass});
        }
        return result;
    }

    @Override
    public RPCResult<Boolean> changePass(String token, String oldPass, String newPass) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            String tokenData = DesDecrypter.decryptString(token, appTokenEncodeKey);
            String[] data = tokenData.split(":");
            String redisTokenKey = MessageFormat.format(LOGIN_TOKEN, data[1]);
            if (!redisTemplate.persist(redisTokenKey)) {
                throw new BizException("token.error", "token错误");
            }
            clientUserInfoService.changePass(token, oldPass, newPass);
            redisTemplate.delete(redisTokenKey);
            redisTemplate.delete(MessageFormat.format(LOGIN_PIN, data[0]));
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setCode("userrpc.changePass.error");
            result.setMessage("修改用户密码失败");
            log.error("userrpc.changePass.error.%s.%s", new Object[]{token, oldPass});
        }
        return result;
    }

    @Override
    public RPCResult<UserDTO> verificationToken(String token) {
        String tokenData = DesDecrypter.decryptString(token, appTokenEncodeKey);
        RPCResult<UserDTO> result = new RPCResult<>();
        String[] data = tokenData.split(":");
        String redisTokenKey = MessageFormat.format(LOGIN_TOKEN, data[1]);
        UserDTO userDTO = (UserDTO) redisTemplate.opsForValue().get(redisTokenKey);
        if (userDTO != null) {
            redisTemplate.expire(redisTokenKey, 1, TimeUnit.DAYS);
            redisTemplate.expire(MessageFormat.format(LOGIN_PIN, userDTO.getPin()), 1, TimeUnit.DAYS);
            result.setData(userDTO);
            (userDTO).setToken(token);
            result.setSuccess(true);
            return result;
        }
        result.setCode("token.error");
        result.setMessage("token失效");
        return result;
    }

    @SuppressWarnings("Duplicates")
    private UserDTO buildToken(ClientUserInfo userInfo) {
        String loginPinKey = MessageFormat.format(LOGIN_PIN, userInfo.getPin());
        Object o = redisTemplate.opsForValue().get(loginPinKey);
        if (o != null) {
            String oldTokenKey = o.toString();
            oldTokenKey = MessageFormat.format(LOGIN_TOKEN, oldTokenKey);
            redisTemplate.delete(oldTokenKey);
            redisTemplate.delete(loginPinKey);
        }
        String newToken = StringUtils.getUUID();
        UserDTO dto = new UserDTO();
        dto.setPin(userInfo.getPin());
        BeanCoper.copyProperties(dto, userInfo);
        dto.setToken(newToken);
        String newTokenKey = MessageFormat.format(LOGIN_TOKEN, newToken);
        redisTemplate.opsForValue().set(loginPinKey, newToken, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(newTokenKey, dto, 1, TimeUnit.DAYS);

        String token = dto.getPin() + ":" + dto.getToken();
        token = DesEncrypter.cryptString(token, appTokenEncodeKey);
        dto.setToken(token);
        return dto;
    }


    @Override
    public RPCResult<Boolean> changePhone(String pin, String phone) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            ClientUserInfo info = clientUserInfoService.findByPin(pin);
            ClientUserInfo upInfo = new ClientUserInfo();
            upInfo.setId(info.getId());
            upInfo.setPhone(phone);
            clientUserInfoService.save(upInfo);
            result.setSuccess(true);
        } catch (DuplicateKeyException e) {
            log.error("change.phone.duplicate.error", e);
            result.setCode("change.phone.duplicate.error");
            result.setMessage("修改电话号码失败,重复");
        } catch (Exception e) {
            log.error("change.phone.error", e);
            result.setCode("change.phone.error");
            result.setMessage("修改电话号码失败");
        }
        return result;
    }

    @Override
    public RPCResult<Boolean> changeSexType(String pin, GenderTypeEnum genderType) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            ClientUserInfo info = clientUserInfoService.findByPin(pin);
            ClientUserInfo upInfo = new ClientUserInfo();
            upInfo.setId(info.getId());
            upInfo.setGenderType(genderType);
            clientUserInfoService.save(upInfo);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("changeSexType.error", e);
            result.setCode("change.sextype.error");
            result.setMessage("修改性别失败");
        }
        return result;
    }

    @Override
    public RPCResult<Boolean> changeNickName(String pin, String nickName) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            ClientUserInfo info = clientUserInfoService.findByPin(pin);
            ClientUserInfo upInfo = new ClientUserInfo();
            upInfo.setId(info.getId());
            upInfo.setNickName(nickName);
            clientUserInfoService.save(upInfo);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("change.nickName.error", e);
            result.setCode("change.sextype.error");
            result.setMessage("修改别名失败");
        }
        return result;
    }

    @Override
    public RPCResult<Boolean> changeSign(String pin, String sign) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            ClientUserInfo info = clientUserInfoService.findByPin(pin);
            ClientUserInfo upInfo = new ClientUserInfo();
            upInfo.setId(info.getId());
            upInfo.setSign(sign);
            clientUserInfoService.save(upInfo);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("change.sign.error", e);
            result.setCode("change.sign.error");
            result.setMessage("修改签名失败");
        }
        return result;
    }


    /**
     * 验证发送短信资数
     *
     * @param key
     */
    private void vSmsTimes(String key) {
        if (redisTemplate.hasKey(key + ".sms.times.max")) {
            throw new BizException("sms.send.over.times", "发送短信超过次数");
        }

        Integer times = (Integer) redisTemplate.opsForValue().get(key + ".sms.times");
        if (times != null && times >= 5) {
            redisTemplate.opsForValue().set(key + ".sms.times.max", 1, 20, TimeUnit.MINUTES);
            throw new BizException("sms.send.over.times", "发送短信超过次数");
        }

        Long increment = redisTemplate.opsForValue().increment(key + ".sms.times", 1);
        if (increment == 1) {
            redisTemplate.expire(key + ".sms.times", 5, TimeUnit.MINUTES);
        }
    }


    @Override
    public RPCResult<Boolean> buildLoginCode(String proxyId, String pin) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            if (!StringUtils.isMobileNO(pin) && !StringUtils.isEmail(pin)) {
                throw new BizException("账号格式不正确");
            }
            String code = StringUtils.randomSixCode();
            String content = MessageFormat.format("您好!您的验证码:{0},有效时间5分钟，请及时验证!", code);
            String buildLoginCodeKey = MessageFormat.format(login_build_code, proxyId, pin);
            vSmsTimes(buildLoginCodeKey);
            redisTemplate.opsForValue().set(buildLoginCodeKey, code);
            result = smsInfoRPCService.buildSMSCode(pin, content, "passport");
            if (!result.getSuccess()) {
                return result;
            }
            result.setSuccess(true);
            return result;
        } catch (BizException e) {
            result.setException(e);
        } catch (Exception e) {
            log.error("buildLoginCode.error", e);
        }
        result.setCode("buildLoginCode.error");
        result.setMessage("生成登录验证码失败");
        return result;
    }


    @Override
    public RPCResult<UserDTO> loginCodeAndVerification(String proxyId, String pin, String code) {
        RPCResult<UserDTO> result = new RPCResult<>();
        try {
            if (StringUtils.isBlank(pin)) {
                result.setCode("data.error");
                result.setMessage("数据失败");
                return result;
            }
            if (StringUtils.isBlank(code)) {
                result.setCode("data.error");
                result.setMessage("数据失败");
                return result;
            }
            String buildLoginCodeKey = MessageFormat.format(login_build_code, proxyId, pin);
            vSmsTimes(buildLoginCodeKey);
            String cacheCode = (String) redisTemplate.opsForValue().get(buildLoginCodeKey);
            if (!code.equalsIgnoreCase(cacheCode)) {
                result.setCode("verification.error");
                result.setMessage("登录验证失败");
                return result;
            }
            ClientUserInfo userInfo = clientUserInfoService.findByPhone(proxyId, pin);
            UserDTO userDTO = buildToken(userInfo);
            result.setData(userDTO);
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            log.error("login.error", e);
            result.setCode("verification.error");
            result.setMessage("登录验证失败");
        }
        return result;
    }

    @Override
    public RPCResult<Boolean> buildRegistCode(String proxyId, String account) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            if (!StringUtils.isMobileNO(account)) {
                throw new BizException("账号格式不正确");
            }
            String code = StringUtils.randomSixCode();
            String content = MessageFormat.format("您好!您的验证码:{0},有效时间5分钟，请及时验证!", code);
            String buildLoginCodeKey = MessageFormat.format(regist_build_code, proxyId, account);
            vSmsTimes(buildLoginCodeKey);
            redisTemplate.opsForValue().set(buildLoginCodeKey, code);
            result = smsInfoRPCService.buildSMSCode(account, content, "passport");
            if (!result.getSuccess()) {
                return result;
            }
            result.setSuccess(true);
            return result;
        } catch (BizException e) {
            result.setException(e);
        } catch (Exception e) {
            log.error("buildLoginCode.error", e);
        }
        result.setCode("buildLoginCode.error");
        result.setMessage("生成登录验证码失败");
        return result;
    }

    @Override
    public RPCResult<UserDTO> registVerification(String proxyId, String upPin, String account, String code, String pass) {
        RPCResult<UserDTO> result = new RPCResult<>();
        try {
            String buildLoginCodeKey = MessageFormat.format(regist_build_code, proxyId, account);
            vSmsTimes(buildLoginCodeKey);
            String cacheCode = (String) redisTemplate.opsForValue().get(buildLoginCodeKey);
            if (!code.equalsIgnoreCase(cacheCode)) {
                result.setCode("verification.error");
                result.setMessage("注册验证失败");
                return result;
            }
            redisTemplate.delete(buildLoginCodeKey);
            ClientUserInfo upUser = clientUserInfoService.findByPin(upPin);
            if (upUser == null) {
                upPin = null;
            }
            if (StringUtils.isBlank(proxyId)) {
                throw new BizException("proxyId.error", "代理商id错误 " + proxyId);
            }
            if (StringUtils.isNotBlank(account) && !StringUtils.isMobileNO(account)) {
                throw new BizException("phone.error", "电话号码错误 " + account);
            }
            ClientUserInfo entity = clientUserInfoService.findByPhone(proxyId, account);
            if (entity != null && entity.getDelStatus()==false) {
                log.error("该账号已经注册过了 " + account);
                throw new BizException("该账号已经注册过");
            }

            if (entity == null) {
                entity = new ClientUserInfo();
            }
            entity.setProxyId(proxyId);
            entity.setPhone(account);
            String nick = account.substring(7);
            entity.setNickName(nick);
            entity.setGenderType(GenderTypeEnum.MALE);
            entity.setPasswd(MD5.MD5Str(pass, passKey));
            entity.setBirthDay(new Date());

            entity.setStatus(true);

            clientUserInfoService.insert(entity);


            String id = entity.getId();

            ClientUserInfo upEntity = new ClientUserInfo();
            upEntity.setId(id);
            String pin = String.valueOf(Integer.parseInt(id));
            upEntity.setPin(pin);
            clientUserInfoService.save(upEntity);
            UserDTO userDTO = buildToken(entity);
            result.setData(userDTO);
            result.setSuccess(true);
            return result;
        } catch (BizException e) {
            result.setException(e);
        } catch (Exception e) {
            log.error("registVerification.error", e);
        }
        result.setCode("buildLoginCode.error");
        result.setMessage("生成登录验证码失败");
        return result;
    }

    @Override
    public RPCResult<Boolean> changeBirthday(String pin, String birthday) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            ClientUserInfo info = clientUserInfoService.findByPin(pin);
            ClientUserInfo upInfo = new ClientUserInfo();
            upInfo.setId(info.getId());
            upInfo.setBirthDay(DateUtil.parseDate(birthday));
            clientUserInfoService.save(upInfo);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("change.changeBirthday.error", e);
            result.setCode("change.changeBirthday.error");
            result.setMessage("修改签名失败");
        }
        return result;
    }

    @Override
    public RPCResult<Boolean> forgetPass(String proxyId, String account) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            if (StringUtils.isBlank(account)) {
                throw new BizException("data.error", "参数不能为空");
            }
            ClientUserInfo userInfo = clientUserInfoService.findByPhone(proxyId, account);
            if (userInfo == null) {
                throw new BizException("无法找到该用户");
            }
            String code = StringUtils.randomSixCode();
            String mobile = userInfo.getPhone();
            String redisKey = MessageFormat.format(forget_pass, proxyId, account);
            vSmsTimes(redisKey);
            redisTemplate.opsForValue().set(redisKey, code);
            String content = MessageFormat.format("您好!您的验证码:{0},有效时间5分钟，请及时验证!", code);
            smsInfoRPCService.buildSMSCode(mobile, content, "passport.forgetPass");
            result.setSuccess(true);
            result.setData(true);
            return result;
        } catch (Exception e) {
            log.error("forgetPass.error", e);
        }
        result.setCode("forgetPass.error");
        result.setMessage("调用忘记密码接口失败");
        return result;
    }


    @Override
    public RPCResult<Boolean> forgetPassCodeVerification(String proxyId, String pin, String code, String pass) {
        RPCResult<Boolean> result = new RPCResult<>();
        try {
            if (StringUtils.isBlank(pin)) {
                throw new BizException("forgetPassCodeVerification.account.blank.error", "电话号码不能为空");
            }
            ClientUserInfo userInfo = clientUserInfoService.findByPhone(proxyId, pin);
            if (userInfo == null) {
                throw new BizException("forgetPassCodeVerification.account.error", "无法找到该用户");
            }
            String key = MessageFormat.format(forget_pass, proxyId, pin);
            String vCode = (String) redisTemplate.opsForValue().get(key);
            if (!vCode.equalsIgnoreCase(code)) {
                throw new BizException("data.verification.error", "验证失败");
            }
            redisTemplate.delete(key);
            clientUserInfoService.changePass(userInfo.getPin(), pass);
            result.setSuccess(true);
            result.setData(true);
            return result;
        } catch (BizException e) {
            result.setException(e);
        } catch (Exception e) {
            log.error("forgetPassCodeVerification.error", e);
        }
        result.setCode("forgetPassCodeVerification.error");
        result.setMessage("调用忘记密码验证失败");
        return result;
    }
}
