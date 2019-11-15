package com.sip.peers.callback;

public interface ReceiveReport {
    /**
     * 接受报告
     * @param worker 员工
     * @param report 报告内容
     */
    public void receiveReport(Worker worker, String report);
}
