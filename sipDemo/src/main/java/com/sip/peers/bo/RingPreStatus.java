package com.sip.peers.bo;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-20 09:47
 */
public class RingPreStatus {

    private String appId;

    private String callId;

    private String msg;

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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public RingPreStatus(String appId, String callId, String msg) {
        this.appId = appId;
        this.callId = callId;
        this.msg = msg;
    }

    public RingPreStatus() {
    }

    @Override
    public String toString() {
        return "CallPreStatus{" +
                "appId='" + appId + '\'' +
                ", callId='" + callId + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
