package com.sip.peers.bo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-13 16:27
 */
@Data
@Document(collection = "sipRingRecord")
public class SipRingRecord implements Serializable {

    private String callId;

    private List<String> requestInfoList;

    private List<String> responseInfoList;

    private Date requestTime;

    private Date responseTime;

    private Date saveTime;

    public SipRingRecord() {
        this.requestInfoList = new ArrayList<>();
        this.responseInfoList = new ArrayList<>();
    }


}
