package com.wxp.dao;

import com.wxp.po.User;
import com.wxp.util.MD5Utils;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsernameAndPassword(String username, String password);

}
