package com.passport.service.rpc;

import com.common.exception.BizException;
import com.common.rpc.StatusRpcServiceImpl;
import com.common.util.RPCResult;
import com.passport.domain.SMSInfo;
import com.passport.rpc.SMSInfoRPCService;
import com.passport.service.SMSInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;

/**
 * Created by dabai on 2017/5/13.
 */

@Service
@org.apache.dubbo.config.annotation.Service
public class SMSInfoRPCServiceImpl extends StatusRpcServiceImpl implements SMSInfoRPCService {

    private static final Logger logger = LoggerFactory.getLogger(SMSInfoRPCServiceImpl.class);

    @Resource
    private SMSInfoService smsInfoService;

    @Override
    public RPCResult<Boolean> buildSMSCode(String mobile, String content, String source) {
        RPCResult<Boolean> result = new RPCResult<>();
        result.setData(false);
        try {
            SMSInfo info = new SMSInfo();
            info.setMobile(mobile);
            info.setContent(content);
            info.setSender(source);
            smsInfoService.insert(info);
            result.setSuccess(true);
        } catch (BizException e) {
            logger.error("发送短信业务异常",e);
            result.setSuccess(false);
            result.setCode(e.getCode());
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("发送短信失败",e);
            result.setSuccess(false);
            result.setCode("send.msg.error");
            result.setMessage("发送短信失败");
        }
        return result;
    }



    /**
     * 获取当前时间到明天凌晨的毫秒数
     *
     * @return
     */
    public static Long getSecond2NextEarlyMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis())/1000;
    }
}
