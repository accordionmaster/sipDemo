package com.sip.peers.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: sipDemo
 * @description: 此对象用于数据持久化,并提供作为查询内容
 * @author: wangxp
 * @create: 2019-11-19 11:12
 */
@Document(collection = "ringresult")
public class RingResult implements Serializable {

    private String appId;

    private String callId;

    private int dir = 0;

    private String serviceId = "0";

    private String ansCode;

    private String event;

    private Date timeStamp;

    private String data;

    public String getAnsCode() {
        return ansCode;
    }

    public void setAnsCode(String ansCode) {
        this.ansCode = ansCode;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public RingResult(String appId, String callId, int dir, String serviceId) {
        this.appId = appId;
        this.callId = callId;
        this.dir = dir;
        this.serviceId = serviceId;
    }

    public RingResult() {
    }

    @Override
    public String toString() {
        return "RingResult{" +
                "appId='" + appId + '\'' +
                ", callId='" + callId + '\'' +
                ", dir=" + dir +
                ", serviceId='" + serviceId + '\'' +
                ", ansCode=" + ansCode +
                ", event='" + event + '\'' +
                ", timeStamp=" + timeStamp +
                ", data='" + data + '\'' +
                '}';
    }
}
