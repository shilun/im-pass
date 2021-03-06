package com.passport.main.controller;

import com.common.annotation.RoleResource;
import com.common.util.BeanCoper;
import com.passport.domain.RoleInfo;
import com.passport.main.AbstractClientController;
import com.passport.main.controller.dto.RoleDto;
import com.passport.service.OperatorLogService;
import com.passport.service.RoleInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(description = "角色管理")
@RestController
@RequestMapping(method = {RequestMethod.POST})
public class RoleInfoController extends AbstractClientController {
    @Resource
    private RoleInfoService roleInfoService;
    @Resource
    private OperatorLogService operatorLogService;

    /**
     * 查询
     *
     * @param info
     * @return
     */
    @RoleResource(resource = "passport")
    @ApiOperation(value = "查询")
    @RequestMapping("/role/list")
    public Map<String, Object> list(@RequestBody RoleDto info) {
        return buildMessage(() -> {
            RoleInfo entity = new RoleInfo();
            BeanCoper.copyProperties(entity, info);
            return roleInfoService.queryByPage(entity, info.getPageinfo().getPageable());
        });
    }

    /**
     * 查询
     *
     * @param params
     * @return
     */
    @ApiOperation(value = "保存")
    @RequestMapping("/role/view")
    @RoleResource(resource = "passport")
    public Map<String, Object> view(@RequestBody Map<String, String> params) {
        return buildMessage(() -> roleInfoService.findById(params.get("id")));
    }

    /**
     * 保存
     *
     * @param info
     * @return
     */
    @ApiOperation(value = "保存")
    @RequestMapping("/role/save")
    @RoleResource(resource = "passport")
    public Map<String, Object> save(@RequestBody RoleDto info) {
        return buildMessage(() -> {
            operatorLogService.logInfo("passport",getPin(),"/role/save", JSONObject.fromObject(info).toString());
            RoleInfo entity = new RoleInfo();
            BeanCoper.copyProperties(entity, info);
            roleInfoService.save(entity);
            return null;
        });
    }
}
