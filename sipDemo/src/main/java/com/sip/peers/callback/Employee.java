package com.sip.peers.callback;

import java.util.Random;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-14 19:21
 */
public class Employee implements Worker {

    private ReceiveReport boss;
    private String name; // 员工姓名

    // 构造器
    public Employee(String name) {
        this.name = name;
    }

    /**
     * 工作
     * @param taskName 任务名称
     */
    @Override
    public void work(String taskName) {
        System.out.println(name + " is doing works:" + taskName);
        Random random = new Random();
        Integer time = random.nextInt(10000);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String report = "顺利完成任务!";
        // 通知老板
        boss.receiveReport(this,report);
    }

    @Override
    public void setReceiveReport(ReceiveReport boss) {
        this.boss = boss;
    }

    @Override
    public void getReward(Double money) {
        System.out.println(name+"由于表现突出, 获得$"+money+"现金奖励!");
    }

    @Override
    public String getName() {
        return name;
    }
}
