package com.passport.proxy.controller;

import com.passport.proxy.AbstractClientController;
import com.passport.rpc.dto.ProxyUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping(method = {RequestMethod.POST})
public class MenuController extends AbstractClientController {
    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    //1运营  2财务  3超级管理员

    @RequestMapping("/menu")
    public Map<String, Object> buildMenu(HttpServletResponse response) {
        return buildMessage(() -> {
            ProxyUserDto proxy = getUserDto();
            if(proxy==null){
                return "";
            }
            if(proxy.getRoles().length==0){
                return "";
            }
            if(proxy.getRoles().length>=3){
                return "3";
            }
            return String.valueOf(proxy.getRoles()[0]);
        });
    }
}
