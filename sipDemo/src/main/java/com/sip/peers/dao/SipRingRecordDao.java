package com.sip.peers.dao;

import com.sip.peers.bo.SipRingRecord;
import org.springframework.data.crossstore.ChangeSetPersister;

public interface SipRingRecordDao {

    void saveSipRingRecord(SipRingRecord ringRecord);

    String getSipRecordByCallId(String callId) throws Exception;
}
