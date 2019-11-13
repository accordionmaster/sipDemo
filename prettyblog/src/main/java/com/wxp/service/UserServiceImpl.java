package com.wxp.service;

import com.wxp.dao.UserRepository;
import com.wxp.po.User;
import com.wxp.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: prettyblog
 * @description:
 * @author: wangxp
 * @create: 2019-11-12 21:55
 */
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Override
    public User checkUser(String userName, String password) {
        User user = userRepository.findByUsernameAndPassword(userName, MD5Utils.code(password));
        return user;
    }
}
