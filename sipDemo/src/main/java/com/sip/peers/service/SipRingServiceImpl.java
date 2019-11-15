package com.sip.peers.service;

import com.sip.peers.bo.SipRingRecord;
import com.sip.peers.dao.SipRingRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-14 11:33
 */
@Service
public class SipRingServiceImpl implements SipRingService {

    @Autowired
    private SipRingRecordDao sipRingRecordDao;

    @Override
    public void saveSipRingRecord(SipRingRecord ringRecord) {
        this.sipRingRecordDao.saveSipRingRecord(ringRecord);
    }

    @Override
    public String getSipRecordByCallId(String callId) throws Exception {
        return this.sipRingRecordDao.getSipRecordByCallId(callId);
    }
}
