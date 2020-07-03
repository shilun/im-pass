package com.passport.proxy.controller;

import com.common.exception.BizException;
import com.common.util.RPCResult;
import com.common.util.StringUtils;
import com.passport.proxy.AbstractClientController;
import com.passport.proxy.controller.dto.ChangePassDto;
import com.passport.proxy.controller.dto.LoginByPassDto;
import com.passport.rpc.dto.ProxyUserDto;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping(value = "/login", method = {RequestMethod.POST})
public class LoginController extends AbstractClientController {

    @RequestMapping("in")
    @ResponseBody
    @ApiOperation(value = "密码登录")
    public Map<String, Object> login(@RequestBody LoginByPassDto dto, HttpServletResponse response) {
        return buildMessage(() -> {
            getRequest().getSession().removeAttribute("userDto");
            String domain = StringUtils.getDomain(getRequest().getRequestURL().toString());
            RPCResult<ProxyUserDto> result = proxyRpcService.login(domain, dto.getAccount(), dto.getPass());
            if (result.getSuccess()) {
                putCookie("pToken", result.getData().getToken(), response);
                return result;
            }
            throw new BizException(result.getCode(), result.getMessage());
        });
    }


    @RequestMapping("loginOut")
    @ResponseBody
    @ApiOperation(value = "登出")
    public Map<String, Object> loginOut() {
        return buildMessage(() -> {
            ProxyUserDto dto = getUserDto();
            if (dto == null) {
                return true;
            }
            return proxyRpcService.logOut(dto.getToken());
        });
    }


    @RequestMapping("changePass")
    @ApiOperation(value = "修改密码")
    @ResponseBody
    public Map<String, Object> changePass(@RequestBody ChangePassDto dto) {
        return buildMessage(() -> {
            return proxyRpcService.changePass(getUserDto().getToken(), dto.getOldPass(), dto.getNewPass());
        });
    }

}
