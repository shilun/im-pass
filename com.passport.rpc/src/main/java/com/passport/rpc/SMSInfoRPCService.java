package com.passport.rpc;

import com.common.rpc.StatusRpcService;
import com.common.util.RPCResult;

/**
 * Created by sl on 05/12/16.
 */
public interface SMSInfoRPCService extends StatusRpcService {

    /**
     * 发送短信
     *
     * @param mobile
     * @param content
     * @param source
     * @return
     */
    RPCResult<Boolean> buildSMSCode(String mobile, String content, String source);
}
