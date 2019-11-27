package com.sip.peers.bo;

import javax.validation.constraints.NotNull;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-19 10:43
 */
public class RingParam {

    @NotNull
    private String appId;   // 应用Id

    @NotNull
    private String caller;  // 主叫号码-显示号码

    @NotNull
    private String called;  // 被叫号码

    private String data;    // 要进行透传得数据  振铃功能用不到

    private Integer Timeout;    // 超时未接听则挂断  建议20-60 之间

    private Integer enableAi;   // 该参数是腾讯云呼叫中心中得参数,此项目中不用,仅用来保持数据结构一致

    private String callId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCalled() {
        return called;
    }

    public void setCalled(String called) {
        this.called = called;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getTimeout() {
        return Timeout;
    }

    public void setTimeout(int timeout) {
        Timeout = timeout;
    }

    public Integer getEnableAi() {
        return enableAi;
    }

    public void setEnableAi(int enableAi) {
        this.enableAi = enableAi;
    }

    public void setTimeout(Integer timeout) {
        Timeout = timeout;
    }

    public void setEnableAi(Integer enableAi) {
        this.enableAi = enableAi;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public RingParam(@NotNull String appId, @NotNull String caller, @NotNull String called, String data, Integer timeout, Integer enableAi, String callId) {
        this.appId = appId;
        this.caller = caller;
        this.called = called;
        this.data = data;
        Timeout = timeout;
        this.enableAi = enableAi;
        this.callId = callId;
    }

    public RingParam() {
    }

    @Override
    public String toString() {
        return "RingParam{" +
                "appId='" + appId + '\'' +
                ", caller='" + caller + '\'' +
                ", called='" + called + '\'' +
                ", data='" + data + '\'' +
                ", Timeout=" + Timeout +
                ", enableAi=" + enableAi +
                ", callId='" + callId + '\'' +
                '}';
    }
}
