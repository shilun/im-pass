package com.passport.api.controller;

import com.common.exception.BizException;
import com.common.util.ContentDto;
import com.common.util.RPCResult;
import com.common.util.StringUtils;
import com.common.web.IExecute;
import com.passport.api.AbstractClientController;
import com.passport.api.controller.dto.*;
import com.passport.domain.model.AgentTypeEnum;
import com.passport.rpc.UserRPCService;
import com.passport.rpc.dto.ProxyDto;
import com.passport.rpc.dto.UserDTO;
import com.passport.service.ClientUserInfoService;
import com.passport.service.SoftWareService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping(value = "/login", method = {RequestMethod.POST})
public class LoginController extends AbstractClientController {

    @Resource
    private UserRPCService userRPCService;

    @Resource
    private ClientUserInfoService clientUserInfoService;

    @Resource
    private SoftWareService softWareService;
    @Value("${app.cookie.encode.key}")
    private String cookieEncodeKey;

    @RequestMapping("in")
    @ResponseBody
    @ApiOperation(value = "密码登录")
    public Map<String, Object> login(@RequestBody LoginByPassDto dto, HttpServletResponse response) {
        return buildMessage(new IExecute() {
            @Override
            public Object getData() {
                getRequest().getSession().removeAttribute("userDto");
                RPCResult<UserDTO> result = userRPCService.login(getDomain().getId(), dto.getAccount(), dto.getPass());
                if (result.getSuccess()) {
                    putCookie("token", result.getData().getToken(), response);
                    return result;
                }
                throw new BizException(result.getCode(), result.getMessage());
            }
        });
    }


    @RequestMapping("buildLoginMobileCode")
    @ResponseBody
    @ApiOperation(value = "手机号登陆获取验证码")
    public Map<String, Object> buildLoginMobileCode(@RequestBody ContentDto dto) {
        return buildMessage(() -> {
            userRPCService.buildLoginCode(getDomain().getId(), dto.getContent());
            return null;
        });
    }

    @RequestMapping("loginMobileCodeVerification")
    @ResponseBody
    @ApiOperation(value = "手机号登陆校验")

    public Map<String, Object> loginMobileCodeVerification(@RequestBody LoginByCodeVerDto dto) {
        return buildMessage(new IExecute() {
            @Override
            public Object getData() {
                return userRPCService.loginCodeAndVerification(getDomain().getId(), dto.getAccount(), dto.getCode());
            }
        });
    }

    @RequestMapping("register")
    @ResponseBody
    @ApiOperation(value = "手机号注册获取验证码")
    public Map<String, Object> register(@RequestBody ContentDto dto) {
        return buildMessage(() -> {
            userRPCService.buildRegistCode(getDomain().getId(), (String) dto.getContent());
            return null;
        });
    }

    @RequestMapping("registVerification")
    @ResponseBody
    @ApiOperation(value = "手机号注册校验")
    public Map<String, Object> registVerification(@RequestBody RegisterVerDto dto) {
        return buildMessage(new IExecute() {
            @Override
            public Object getData() {
                return userRPCService.registVerification(getDomain().getId(),dto.getUpPin(), dto.getAccount(), dto.getCode(), dto.getPass());
            }
        });
    }

    @RequestMapping("loginOut")
    @ResponseBody
    @ApiOperation(value = "登出")
    public Map<String, Object> loginOut() {
        return buildMessage(() -> {
            UserDTO dto = getUserDto();
            if (dto == null) {
                return true;
            }
            return userRPCService.loginOut(dto.getToken());
        });
    }


    @RequestMapping("changePass")
    @ApiOperation(value = "修改密码")
    @ResponseBody
    public Map<String, Object> changePass(@RequestBody ChangePassDto dto) {
        return buildMessage(() -> {
            userRPCService.changePass(getUserDto().getPin(), dto.getOldPass(), dto.getNewPass());
            return null;
        });
    }

    @RequestMapping("changeNick")
    @ApiOperation(value = "修改昵称")
    @ResponseBody
    public Map<String, Object> changeNick(@RequestBody ChangeNickDto dto) {
        return buildMessage(() -> {
            userRPCService.changeNickName(getUserDto().getPin(), dto.getNick());
            return null;
        });
    }

    @RequestMapping("changeSex")
    @ApiOperation(value = "修改性别")
    @ResponseBody
    public Map<String, Object> changeSex(@RequestBody ChangeSexDto dto) {
        return buildMessage(() -> {
            userRPCService.changeSexType(getUserDto().getPin(), dto.getGenderType());
            return null;
        });
    }

    @RequestMapping("changeBirthday")
    @ApiOperation(value = "修改生日")
    @ResponseBody
    public Map<String, Object> changeBirthday(@RequestBody String birthday) {
        return buildMessage(() -> {
            userRPCService.changeBirthday(getUserDto().getPin(), JSONObject.fromObject(birthday).getString("birthday"));
            return null;
        });
    }

    @RequestMapping("forgetPass")
    @ApiOperation(value = "忘记密码获取验证码")
    @ResponseBody
    public Map<String, Object> forgetPass(@RequestBody ContentDto dto) {
        return buildMessage(() -> {
            userRPCService.forgetPass(getDomain().getId(), dto.getContent());
            return null;
        });
    }

    @RequestMapping("forgetPassVer")
    @ApiOperation(value = "忘记密码校验")
    @ResponseBody
    public Map<String, Object> forgetPassVer(@RequestBody ForgetPassVerDto dto) {
        return buildMessage(() -> {
            userRPCService.forgetPassCodeVerification(getDomain().getId(), dto.getAccount(), dto.getCode(), dto.getPass());
            return null;
        });
    }

    @ApiOperation(value = "android地址")
    @ResponseBody
    @RequestMapping(value = "androidDown", method = {RequestMethod.GET})
    public Map<String, Object> androidDown() {
        return buildMessage(() -> {
            return softWareService.findLastInfo(getDomain().getId(), AgentTypeEnum.Android).getUrl();
        });
    }

    @RequestMapping(value = "reg", method = {RequestMethod.GET})
    @ApiOperation(value = "用户注册")
    public String reg(String q, Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("recommendId", q);
        String domain = StringUtils.getDomain(getRequest().getRequestURL().toString());
        ProxyDto domain1 = getDomain();
        String[] domains = domain1.getDomain();
        if (true == domain1.getSelfReg()) {
            if (domains.length >= 2 && domains[0].equals(domain)) {
                model.addAttribute("url", "http://passport." + domains[1] + "/ui/?q=" + q);
                return "/redirectUrl";
            }
            if (domains.length == 1) {
                model.addAttribute("url", "http://passport." + domains[0] + "/ui/?q=" + q);
                return "/redirectUrl";
            }
            return "redirect:http://passport." + domains[1] + "/ui/?q=" + q;
        }
        if (isWechat()) {
            model.addAttribute("url", "http://passport." + domains[0] + "/login/reg?q=" + q);
            return "/intercept";
        }
        if (domains.length >= 2 && domains[0].equals(domain)) {
            model.addAttribute("url", "http://passport." + domains[1] + "/login/reg?q=" + q);
            return "/redirectUrl";
        }
        AgentTypeEnum agentType = getAgentType();
        if (agentType == AgentTypeEnum.Android || agentType == AgentTypeEnum.Other) {
            model.addAttribute("url", softWareService.findLastInfo(getDomain().getId(), AgentTypeEnum.Android).getUrl());
        }
        model.addAttribute("agentType", agentType.name());
        return "/register";
    }

    public boolean isWechat() {
        String ua = getRequest().getHeader("User-Agent").toLowerCase();
        if (ua.indexOf("micromessenger") > -1) {
            return true;
        }
        return false;
    }


    @RequestMapping(value = "proios", method = {RequestMethod.GET})
    @ApiOperation(value = "IOS用户注册提示")
    public String proIOS(Model model) {
        String domain = StringUtils.getDomain(getRequest().getRequestURL().toString());
        model.addAttribute("url", "itms-services://?action=download-manifest&url=https://passport." + domain + "/AppDownload/download.plist");
        return "/promptIOS";
    }


    public AgentTypeEnum getAgentType() {
        String agent = getRequest().getHeader("user-agent").toLowerCase();
        if (agent.indexOf(AgentTypeEnum.Android.name().toLowerCase()) != -1) {
            return AgentTypeEnum.Android;
        }
        if (agent.indexOf("iPhone".toLowerCase()) != -1 || agent.indexOf("iPod".toLowerCase()) != -1 || agent.indexOf("iPad".toLowerCase()) != -1) {
            return AgentTypeEnum.IOS;
        }
        return AgentTypeEnum.Other;
    }

}
