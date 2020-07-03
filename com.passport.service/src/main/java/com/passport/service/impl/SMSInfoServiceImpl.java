package com.passport.service.impl;

import com.common.exception.BizException;
import com.common.mongo.AbstractMongoService;
import com.common.util.DateUtil;
import com.passport.domain.SMSInfo;
import com.passport.service.SMSInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 */
@Service
public class SMSInfoServiceImpl extends AbstractMongoService<SMSInfo> implements SMSInfoService {

    private static final Logger logger = LoggerFactory.getLogger(SMSInfoServiceImpl.class);

    @Override
    protected Class getEntityClass() {
        return SMSInfo.class;
    }


    private final String PASS_SEND_COUNT = "passport.send.sms.count.{0}";
    private final String LAST_SEND_SMS_TIME = "passport.send.sms.last.time.{0}";
    @Resource
    private RedisTemplate redisTemplate;

//    @Value("${app.sms.limit.daytotal}")
    private Integer smsDayCount=5;

    /**
     * 校验短信合法
     * @param info
     */
    private void checkSms(SMSInfo info) {
        String key = MessageFormat.format(LAST_SEND_SMS_TIME, info.getMobile());
        long now = System.currentTimeMillis();
        if (redisTemplate.hasKey(key)) {
            throw new BizException("发送短信过于频繁");
        } else {
            redisTemplate.opsForValue().set(key, now, 1, TimeUnit.MINUTES);
        }
    }

    @Override
    public void insert(SMSInfo entity) {
        checkSms(entity);

        String key = MessageFormat.format(PASS_SEND_COUNT, entity.getMobile());
        Date startDate = DateUtil.getStartDate(new Date());
        Date endDate = DateUtil.getEndDate(new Date());
        SMSInfo query = new SMSInfo();
        query.setMobile(entity.getMobile());
        query.setStartCreateTime(startDate);
        query.setEndCreateTime(endDate);
        Integer countTotal = (Integer) redisTemplate.opsForValue().get(key);
        if (countTotal != null) {
            if (countTotal < smsDayCount) {
                incr(key, endDate.getTime() - System.currentTimeMillis());
            } else {
                throw new BizException("passport.sms.day.limit", "短信日限量");
            }
        } else {
            incr(key, endDate.getTime() - System.currentTimeMillis());
        }
        super.insert(entity);
    }

    /**
     * @param key
     * @param liveTime
     * @return
     */
    public Long incr(String key, long liveTime) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.getAndIncrement();

        if ((null == increment || increment.longValue() == 0) && liveTime > 0) {//初始设置过期时间
            entityIdCounter.expire(liveTime, TimeUnit.MILLISECONDS);
        }

        return increment;
    }
}
