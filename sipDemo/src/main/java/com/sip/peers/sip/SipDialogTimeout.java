package com.sip.peers.sip;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;

/**
 * @program: sipDemo
 * @description: 超时处理线程
 * @author: wangxp
 * @create: 2019-11-26 16:30
 */
public abstract class SipDialogTimeout implements Runnable {

    protected TimeoutEvent evt;
    protected Dialog dialog;
    protected boolean isReturn;

    @Override
    public void run() {
        if (evt.isServerTransaction()){
            ServerTransaction serverTransaction = evt.getServerTransaction();
            if (serverTransaction != null){
                dialog = serverTransaction.getDialog();
            }
            isReturn = true;
        } else {
            ClientTransaction clientTransaction = evt.getClientTransaction();
            if (clientTransaction != null){
                dialog = clientTransaction.getDialog();
            }
            isReturn = true;
        }
        if (null != dialog){
            onProcess(isReturn);
        }
    }

    protected abstract void onProcess(boolean isReturn);

    public void setParam(TimeoutEvent evt){
        this.evt = evt;
    }
}
