package com.wxp.service;

import com.wxp.po.User;

public interface UserService {

    User checkUser(String username, String password);
}
