package com.sip.peers.redisQueue;

import com.google.gson.Gson;
import com.sip.peers.bo.RingParam;
import com.sip.peers.sip.SipLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sip.*;
import javax.xml.soap.MessageFactory;
import java.text.ParseException;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-21 11:14
 */
@RestController
@Controller
public class RedisConsumerController {

    @Autowired
    RedisClient redisClient;

    private final static String MESSAGE = "testmq";

    /**
     * 接收消息API  使用定时功能,判断redis队列中是否存有请求信息,如果有,则将请求数据取出,并发送请求.
     * @return
     */
    // @Scheduled(cron = "*/5 * * * * ?")      // 暂时设置为每隔5秒访问一次消息队列.  可以根据实际需要修改访问时间间隔
    public void sendMessage() throws InvalidArgumentException, TransportNotSupportedException, TooManyListenersException, PeerUnavailableException, ObjectInUseException {

        int size = redisClient.lrange(MESSAGE, 0, -1).size();
        System.out.println("Redis 数据库中请求个数为: " + size);

        if (redisClient.lrange(MESSAGE,0,-1).size() > 0){

            String str = (String) redisClient.brpop(MESSAGE, 0, TimeUnit.SECONDS);
            Gson gson = new Gson();
            RingParam ringParam = gson.fromJson(str, RingParam.class);

            String caller = ringParam.getCaller();
            String called = ringParam.getCalled();
            String callId = ringParam.getCallId();
            SipLayer sip = new SipLayer("caller","192.168.200.1",64526);
//        String called = "<sip:+86"+called+"@chinamobile.com>";
            ringParam.setCalled("<sip:some@192.168.200.1:56205>");
            Date date = new Date();
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
        }
    }
}
