package com.sip.peers.service;

import com.sip.peers.bo.SipRingRecord;
import org.springframework.data.crossstore.ChangeSetPersister;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-14 11:31
 */
public interface SipRingService {

    public void saveSipRingRecord(SipRingRecord ringRecord);

    public String getSipRecordByCallId(String callId) throws Exception;
}
