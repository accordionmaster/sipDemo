package com.sip.peers.redisQueue;

import com.sip.peers.bo.RingParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @program: sipDemo
 * @description: 生产者(消息发送方)
 * @author: wangxp
 * @create: 2019-11-21 10:54
 */
@RestController
@RequestMapping("/producer")
public class RedisProducerController {

    @Autowired
    RedisClient redisClient;

    /** 公共配置 */
    private final static String SUCCESS = "success";
    private final static String MESSAGE = "testmq";
    private static final List<String> list;

    static {
        list = Arrays.asList(new String[]{"猿医生", "CD", "YYS"});
    }

    @RequestMapping("/sendMessage")
    public String sendMessage(){
        for (String message : list){
            redisClient.lpush(MESSAGE, message);
        }
        return SUCCESS;
    }
}
