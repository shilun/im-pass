package com.passport.service.impl;

import com.common.exception.BizException;
import com.common.mongo.AbstractMongoService;
import com.common.security.MD5;
import com.common.util.StringUtils;
import com.passport.domain.ClientUserInfo;
import com.passport.rpc.SMSInfoRPCService;
import com.passport.service.ClientUserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

@Service
public class ClientUserInfoServiceImpl extends AbstractMongoService<ClientUserInfo> implements ClientUserInfoService {
    private static Logger logger = LoggerFactory.getLogger(ClientUserInfoServiceImpl.class);

    private final String CLIENT_USER_CACHE = "passport.cache.{0}";
    /**
     * 用户session   时间时长
     */
    public final static int USER_SESSION_TIME = 60 * 24 * 30;
    @Value("${app.passKey}")
    private String passKey;

    @Value("${app.token.encode.key}")
    private String appTokenEncodeKey;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SMSInfoRPCService smsInfoRPCService;

    protected Class<ClientUserInfo> getEntityClass() {
        return ClientUserInfo.class;
    }


    public ClientUserInfo login(Long proxyId, String loginName, String passwd, String ip) {
        if (StringUtils.isBlank(loginName) || StringUtils.isBlank(passwd) || proxyId == null) {
            return null;
        }
        ClientUserInfo query = new ClientUserInfo();
        query.setPasswd(MD5.MD5Str(passwd, passKey));
        query.setStatus(true);
        query.setDelStatus(false);
        boolean account = false;
        if (StringUtils.isMobileNO(loginName)) {
            account = true;
            query.setPhone(loginName);
        }
        if (!account) {
            query.setPin(loginName);
        }
        query = findByOne(query);
        if (query == null) {
            return null;
        }
        return query;
    }

    public ClientUserInfo findByPin(String pin) {
        String userKey = MessageFormat.format(CLIENT_USER_CACHE, pin);
        ClientUserInfo user = (ClientUserInfo) redisTemplate.opsForValue().get(userKey);
        if (user == null) {
            ClientUserInfo query = new ClientUserInfo();
            query.setPin(pin);
            user = findByOne(query);
            if (user != null) {
                redisTemplate.opsForValue().set(userKey, user, USER_SESSION_TIME, TimeUnit.MINUTES);
            }
            return user;
        } else {
            return user;
        }
    }

    public ClientUserInfo findByPhone(String proxyId, String phone) {
        ClientUserInfo query = new ClientUserInfo();
        query.setPhone(phone);
        query.setProxyId(proxyId);
        query.setDelStatus(Boolean.FALSE);
        query = findByOne(query);
        return query;
    }

    public void initPass(String pin, String pwd) {
        ClientUserInfo byPin = findByPin(pin);
        String id = byPin.getId();
        ClientUserInfo info = new ClientUserInfo();
        info.setPasswd(MD5.MD5Str(pwd, passKey));
        info.setId(id);
        try {
            String userKey = MessageFormat.format(CLIENT_USER_CACHE, pin);
            redisTemplate.delete(userKey);
        } catch (Exception e) {
            logger.error("删除用户缓存失败", e);
        }
        up(info);
    }

    //
//    @Override
//    public ClientUserInfo regist(String upPin, String account, String pass) {
//        if (StringUtils.isNotBlank(upPin)) {
//            throw new BizException("regist.user.error.upPin.error");
//        } else {
//            ClientUserInfo info = new ClientUserInfo();
//            info.setPin(upPin);
//            info.setStatus(true);
//            info = findByOne(info);
//            if (info == null) {
//                throw new BizException("regist.user.error.upPin.error", "注册失败，请检查代理用户");
//            }
//            String proxyId = info.getProxyId();
//            info = new ClientUserInfo();
//            info.setProxyId(proxyId);
//            info.setPhone(account);
//            info.setPasswd(MD5.MD5Str(pass, passKey));
//            info.setStatus(true);
//            insert(info);
//            String id = info.getId();
//
//            ClientUserInfo upEntity = new ClientUserInfo();
//            upEntity.setId(id);
//            String pin = String.valueOf(Integer.parseInt(id));
//            upEntity.setPin(pin);
//            up(upEntity);
//            if (messageSender != null) {
//                info.setPin(pin);
//                info.setPasswd(null);
//                messageSender.sendMessage(JSONObject.fromObject(info));
//            }
//            return info;
//        }
//    }
//
//
    @Override
    public ClientUserInfo register(String proxyId, String pin, String pass) {
        ClientUserInfo info = new ClientUserInfo();
        info.setProxyId(proxyId);
        if (StringUtils.isMobileNO(pin)) {
            info.setPhone(pin);
        }

        ClientUserInfo entity = findByOne(info);
        if (entity != null) {
            throw new BizException("user.exist", "用户已经存在");
        }

        if (StringUtils.isEmail(pin)) {
            info.setEmail(pin);
        }
        entity = findByOne(info);
        if (entity != null) {
            throw new BizException("user.exist", "用户已经存在");
        }
        info.setPasswd(MD5.MD5Str(pass, passKey));
        info.setStatus(true);
        insert(info);

        String id = info.getId();
        ClientUserInfo upEntity = new ClientUserInfo();
        upEntity.setId(id);
        upEntity.setPin(pin);
        up(upEntity);
        return info;
    }

    @Override
    public void changePass(String pin, String oldPass, String newPass) {
        ClientUserInfo info = new ClientUserInfo();
        info.setPin(pin);
        info = findByOne(info);
        if (info != null) {
            String id = info.getId();
            if (info.getPasswd().equals(MD5.MD5Str(oldPass, passKey))) {
                info = new ClientUserInfo();
                info.setId(id);
                info.setPasswd(MD5.MD5Str(newPass, passKey));
                up(info);
            } else {
                throw new BizException("oldPass.error");
            }
        }
    }

    @Override
    public void changePass(String pin, String newPass) {
        ClientUserInfo info = new ClientUserInfo();
        info.setPin(pin);
        info = findByOne(info);
        if (info != null) {
            String id = info.getId();
            info = new ClientUserInfo();
            info.setId(id);
            info.setPasswd(MD5.MD5Str(newPass, passKey));
            up(info);
        }
    }

}
