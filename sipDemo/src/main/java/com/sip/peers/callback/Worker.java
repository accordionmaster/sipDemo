package com.sip.peers.callback;

/**
 * @program: sipDemo
 * @description: 工人接口
 * @author: wangxp
 * @create: 2019-11-14 19:18
 */
public interface Worker {
    public void work(String taskName);
    public void setReceiveReport(ReceiveReport boss);
    public void getReward(Double money);
    public String getName();
}
