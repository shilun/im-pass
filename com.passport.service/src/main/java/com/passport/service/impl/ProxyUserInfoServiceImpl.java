package com.passport.service.impl;

import com.common.exception.BizException;
import com.common.mongo.AbstractMongoService;
import com.common.security.MD5;
import com.common.util.StringUtils;
import com.passport.domain.ProxyInfo;
import com.passport.domain.ProxyUserInfo;
import com.passport.service.ProxyInfoService;
import com.passport.service.ProxyUserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProxyUserInfoServiceImpl extends AbstractMongoService<ProxyUserInfo> implements ProxyUserInfoService {

    @Override
    protected Class getEntityClass() {
        return ProxyUserInfo.class;
    }

    @Resource
    private ProxyInfoService proxyInfoService;

    @Value("${app.passKey}")
    private String passKey;

    @Override
    public ProxyUserInfo findByProxyIdAndPin(String proxyId, String pin) {
        ProxyUserInfo info = new ProxyUserInfo();
        info.setPin(pin);
        info.setStatus(true);
        info.setProxyId(proxyId);
        return findByOne(info);
    }

    @Override
    public ProxyUserInfo login(String domain, String account, String pass) {
        ProxyInfo proxy = proxyInfoService.findByDomain(domain);
        pass = MD5.MD5Str(pass, passKey);
        ProxyUserInfo info = new ProxyUserInfo();
        info.setPass(pass);
        info.setPin(account);
        info.setStatus(true);
        info.setProxyId(proxy.getId());
        info = findByOne(info);
        if (info == null) {
            throw new BizException("login.error", "登录失败");
        }
        return info;
    }

    @Override
    public void addUser(String proxyId, String pin, String phone, String pass, String desc, Long[] resources) {
        if (StringUtils.isBlank(proxyId)) {
            throw new BizException("proxyId.error");
        }
        if (StringUtils.isBlank(pin)) {
            throw new BizException("pin.error");
        }
        if (StringUtils.isBlank(phone)) {
            throw new BizException("phone.error");
        }
        if (StringUtils.isBlank(pass)) {
            throw new BizException("pass.error");
        }
        if (StringUtils.isBlank(desc)) {
            throw new BizException("desc.error");
        }
        ProxyUserInfo entity = new ProxyUserInfo();
        entity.setProxyId(proxyId);
        entity.setPin(pin);
        entity.setPhone(phone);
        entity.setPass(pass);
        entity.setDesc(desc);
        entity.setRoles(resources);
        insert(entity);
    }

    @Override
    public void initPass(String proxyId, String pin, String pass) {
        ProxyUserInfo info = findByProxyIdAndPin(proxyId, pin);
        ProxyUserInfo up = new ProxyUserInfo();
        up.setId(info.getId());
        up.setPass(MD5.MD5Str(pass, passKey));
        save(up);
    }

    @Override
    public void upUser(String proxyId, String pin, String phone, String desc, boolean status, Long[] resources) {
        ProxyUserInfo info = findByProxyIdAndPin(proxyId, pin);
        info.setRoles(resources);
        info.setStatus(status);
        info.setPhone(phone);
        info.setDesc(desc);
        save(info);
    }

    @Override
    public void delById(String proxyId, String id) {
        ProxyUserInfo info = findById(id);
        if (!info.getProxyId().equalsIgnoreCase(proxyId)) {
            throw new BizException("data.error", "数据失败");
        }
        delById(id);
    }


    @Override
    public void changePass(String id, String password, String vpassword) {
        ProxyUserInfo query = new ProxyUserInfo();
        query.setId(id);
        query = findByOne(query);
        if (query == null) {
            throw new BizException("user.password.error", "用户不存在");
        }
        if (!password.equals(vpassword)) {
            throw new BizException("user.password.error", "密码和验证密码不一至");
        }
        ProxyUserInfo upEntity = new ProxyUserInfo();
        upEntity.setId(query.getId());
        upEntity.setPass(MD5.MD5Str(password, passKey));
        save(upEntity);
    }
}
