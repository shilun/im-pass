package com.passport.service.impl;

import com.common.mongo.AbstractMongoService;
import com.passport.domain.SoftWare;
import com.passport.domain.model.AgentTypeEnum;
import com.passport.domain.model.VersionTypeEnum;
import com.passport.service.SoftWareService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SoftWareServiceImpl extends AbstractMongoService<SoftWare> implements SoftWareService {
    @Override
    protected Class getEntityClass() {
        return SoftWare.class;
    }

    @Override
    public SoftWare findLastInfo(String proxyId, AgentTypeEnum type) {
        SoftWare query = new SoftWare();
        query.setProxyId(proxyId);
        query.setOsType(type);
        query.setStatus(true);
        query.setVersionType(VersionTypeEnum.Full);
        Page<SoftWare> softWares = queryByPage(query, PageRequest.of(0, 1));
        return softWares.getContent().get(0);
    }
}
