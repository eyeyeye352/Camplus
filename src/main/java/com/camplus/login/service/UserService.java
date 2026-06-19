package com.camplus.login.service;

import com.camplus.login.entity.*;


public interface UserService {

    /**
     * 用户注册
     * user 待注册用户对象
     * true-注册成功  false-注册失败
     */
    boolean register(User user);

    /**
     * 用户注册并返回用户对象
     * @param user 待注册用户对象
     * @return 注册成功返回用户对象，失败返回null
     */
    User registerAndReturnUser(User user);

    /**
     * 通用登录
     * @param loginAccount 登录账号(用户名/邮箱/手机号)
     * @param password 明文密码
     * @return 登录成功返回用户对象，失败返回null
     */
    User login(String loginAccount, String password);
}