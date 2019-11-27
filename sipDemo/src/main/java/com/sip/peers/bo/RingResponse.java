package com.sip.peers.bo;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-19 10:49
 */

/**
 * 此对象用于返回给广汽异步回调接口
 */
public class RingResponse {

    private RingResponseCodeEnum code;

    private String appId;

    private String callId;

    public RingResponseCodeEnum getCode() {
        return code;
    }

    public void setCode(RingResponseCodeEnum code) {
        this.code = code;
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

    public RingResponse(RingResponseCodeEnum code, String appId, String callId) {
        this.code = code;
        this.appId = appId;
        this.callId = callId;
    }

    public RingResponse() {
    }

    @Override
    public String toString() {
        return "RingResponse{" +
                "code=" + code +
                ", appId='" + appId + '\'' +
                ", callId='" + callId + '\'' +
                '}';
    }
}
