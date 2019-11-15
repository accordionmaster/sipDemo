package com.sip.peers.callback;


import java.util.ArrayList;
import java.util.List;

// 定义一个Boss类实现这个接收报告的接口
public class Boss implements ReceiveReport{

    private List<Worker> workers = new ArrayList<>(); // 老板管理的员工
    private volatile int index;

    /**
     * 添加员工
     * @param worker 员工
     */
    public void addWorker(Worker worker){
        workers.add(worker);
        worker.setReceiveReport(this);
    }

    /**
     * 下达任务
     * @param task
     */
    public void sendTask(String task){
        // 给各个员工依次下任务
        for(Worker w: workers){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    w.work(task);
                }
            }).start();
        }
    }

    /**
     * 接收报告
     * @param worker 员工
     * @param report 报告内容
     */
    public void receiveReport(Worker worker, String report){
        int index = ++this.index;
        System.out.println(worker.getName()+"获得第"+index+"名");
        if (index <= 3){
            // 给前三名发奖金
            worker.getReward(1000.0*(4-index));
        }
    }

}
