package com.sip.peers.util;

import com.sip.peers.service.SipRingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-14 11:28
 */
@Component
public class InjectServiceUtil {

    @Autowired
    private SipRingService sipRingService;
//    @Autowired
//    private SysRoleUserService2 sysRoleUserService2;

    @PostConstruct
    public void init(){
        InjectServiceUtil.getInstance().sipRingService = this.sipRingService;
//        InjectServiceUtil.getInstance().sysRoleUserService2 = this.sysRoleUserService2;
    }

    /**
     *  实现单例 start
     */
    private static class SingletonHolder {
        private static final InjectServiceUtil INSTANCE = new InjectServiceUtil();
    }
    private InjectServiceUtil (){}
    public static final InjectServiceUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }
    /**
     *  实现单例 end
     */
    public SipRingService getSipRingService(){
        return InjectServiceUtil.getInstance().sipRingService;
    }
//    public SysRoleUserService2 getSysRoleUserService2(){
//        return InjectServiceUtil.getInstance().sysRoleUserService2;
//    }

}
