package com.sip.peers.sip;

import javax.sip.RequestEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.message.Request;

/**
 * @program: sipDemo
 * @description:  请求处理线程
 * @author: wangxp
 * @create: 2019-11-26 16:24
 */
public abstract class SipProcessReqThread implements Runnable {

    protected SipListener sipListener;

    protected Request req;

    protected RequestEvent evt;

    protected SipProvider sipProvider;

    protected String listenerName;

    @Override
    public void run() {
        if (null != req){
            onPress();
        }
    }

    protected abstract void onPress();

    public void setParam(SipListener sipListener, Request req, RequestEvent evt, SipProvider sipProvider, String listenerName){
        this.sipListener = sipListener;
        this.req = req;
        this.evt = evt;
        this.sipProvider = sipProvider;
        this.listenerName = listenerName;
    }
}
