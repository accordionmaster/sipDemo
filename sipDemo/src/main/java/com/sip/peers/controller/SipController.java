package com.sip.peers.controller;

import com.sip.peers.bo.SipRingRecord;
import com.sip.peers.service.SipRingService;
import com.sip.peers.sip.MessageProcessor;
import com.sip.peers.sip.SipLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sip.*;
import java.text.ParseException;
import java.util.TooManyListenersException;

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

    @PostMapping("/ring")
    public void RingController() throws InvalidArgumentException, SipException, TooManyListenersException, ParseException {
        SipLayer sip = new SipLayer("bob","192.168.200.1",64525);
        String too = "<sip:some@192.168.200.1:50047>";
        String messagee = "hello";
        sip.sendMessage(too,messagee);
    }


    @PostMapping("/call")
    @ResponseBody
    public void CallController() throws InvalidArgumentException, SipException, TooManyListenersException, ParseException {
        SipLayer sip = new SipLayer("bob","192.168.200.1",64526);
        String too = "<sip:some@192.168.200.1:53761>";
        String messagee = "hello";
        sip.callRing(too,messagee);
    }


    @GetMapping("/get/{callId}")
    @ResponseBody
    public String getRingRecordByCallId(@PathVariable("callId") String callId) throws Exception {
        return this.sipRingService.getSipRecordByCallId(callId);
    }

}
