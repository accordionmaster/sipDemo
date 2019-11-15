package com.sip.peers.callback;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-14 19:24
 */
public class Test {
    public static void main(String[] args) {
        // 定义一个Boss
        Boss boss = new Boss(); // 定义一个Boss
        // 定义十个员工
        for(int i = 0; i < 10; i++){
            Worker worker = new Employee("Employee["+i+"]");
            boss.addWorker(worker);
        }
        // boss开始下达任务
        boss.sendTask("Say Hello");
    }
}
