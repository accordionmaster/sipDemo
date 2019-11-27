package com.sip.peers.controller;

import com.google.gson.Gson;
import com.sip.peers.bo.RingParam;
import com.sip.peers.bo.RingPreStatus;
import com.sip.peers.redisQueue.RedisClient;
import com.sip.peers.service.SipRingService;
import com.sip.peers.sip.SipLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sip.*;
import java.text.ParseException;
import java.util.*;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-13 13:17
 */
@Controller
public class SipController {

    @Autowired
    SipRingService sipRingService;

    @Autowired
    RedisClient redisClient;

    @Value("${sip.caller.server}")
    private String callerServer;

    @Value("${sip.caller.port}")
    private Integer callerPort;

    @Value("${sip.caller.username}")
    private String callerUsername;

    @Value("${sip.callee.server}")
    private String calleeServer;

    @Value("${sip.callee.port}")
    private Integer calleePort;

    @Value("${sip.callee.username}")
    private String calleeUsername;

    /** 公共配置 */
    private final static String SUCCESS = "success";
    private final static String MESSAGE = "testmq";

    @PostMapping("/call/outCall")
    @ResponseBody
    public RingPreStatus CallController(@RequestBody RingParam ringParam) throws InvalidArgumentException, TransportNotSupportedException, TooManyListenersException, PeerUnavailableException, ObjectInUseException {
        // 把请求放入到消息队列中
        String callId = UUID.randomUUID().toString();
        ringParam.setCallId(callId);
        Gson gson = new Gson();
        String json = gson.toJson(ringParam);
        redisClient.lpush(MESSAGE, json);

        // 提前返回收到请求的回应
        RingPreStatus callStatus = new RingPreStatus();
        callStatus.setAppId(ringParam.getAppId());
        callStatus.setCallId(callId);
        callStatus.setMsg("收到呼叫请求");
        return callStatus;

    }


    @PostMapping("/call/outCall2")
    @ResponseBody
    public RingPreStatus CallController2(@RequestBody RingParam ringParam) throws InvalidArgumentException, TransportNotSupportedException, TooManyListenersException, PeerUnavailableException, ObjectInUseException {

        String callId = UUID.randomUUID().toString();
        String caller = ringParam.getCaller();
        String called = ringParam.getCalled();
        SipLayer sip = new SipLayer(callerUsername,callerServer,callerPort);
//        String called = "<sip:+86"+called+"@chinamobile.com>";
        ringParam.setCalled("<sip:some@192.168.200.1:60449>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sip.callRing(callId, ringParam);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                } catch (SipException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // 提前返回收到请求的回应
        RingPreStatus callStatus = new RingPreStatus();
        callStatus.setAppId(ringParam.getAppId());
        callStatus.setCallId(callId);
        callStatus.setMsg("收到呼叫请求");
        return callStatus;
    }


    @GetMapping("/get/{callId}")
    @ResponseBody
    public String getRingRecordByCallId(@PathVariable("callId") String callId) throws Exception {
        return this.sipRingService.getSipRecordByCallId(callId);
    }

}
