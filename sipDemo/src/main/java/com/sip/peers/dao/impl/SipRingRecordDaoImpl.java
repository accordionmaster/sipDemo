package com.sip.peers.dao.impl;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sip.peers.bo.SipRingRecord;
import com.sip.peers.dao.SipRingRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-13 16:57
 */
@Repository
public class SipRingRecordDaoImpl implements SipRingRecordDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveSipRingRecord(SipRingRecord ringRecord) {
        ringRecord.setSaveTime(new Date());
        mongoTemplate.save(ringRecord);
    }

    @Override
    public String getSipRecordByCallId(String callId) throws Exception {
        SipRingRecord sipRingRecord = null;
        Query query = new Query(Criteria.where("callId").is(callId));
        sipRingRecord = mongoTemplate.findOne(query, SipRingRecord.class);
        return sipRingRecord.toString();

    }

}