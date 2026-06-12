package com.camplus.login.service.impl;

import com.camplus.login.mappers.UserMapper;
import com.camplus.login.entity.User;
import com.camplus.login.service.UserService;
import com.camplus.login.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 密码错误最大次数，超过则锁定账号
    private static final int MAX_ERROR_COUNT = 5;
    // 账号锁定时长（分钟）
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public boolean register(User user) {
        // 校验用户名、邮箱、手机号是否已存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            return false;
        }
        existUser = userMapper.selectByEmail(user.getEmail());
        if (existUser != null) {
            return false;
        }
        existUser = userMapper.selectByPhone(user.getPhone());
        if (existUser != null) {
            return false;
        }

        // 初始化新用户默认参数
        user.setRole(0);
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setLockTime(null);
        user.setLastLoginTime(null);

        // 密码加密
        String encryptPwd = MD5Util.md5Encrypt(user.getPasswordHash());
        user.setPasswordHash(encryptPwd);

        // 使用其它注册方式时，确保username不为null
        if (user.getUsername() == null || "".equals(user.getUsername())) {
            if (user.getEmail() != null && !"".equals(user.getEmail())) {
                // 邮箱注册：用邮箱作为用户名
                user.setUsername(user.getEmail());
            } else if (user.getPhone() != null && !"".equals(user.getPhone())) {
                // 手机号注册：用手机号作为用户名
                user.setUsername(user.getPhone());
            }
        }

        // 插入数据
        int rows = userMapper.insertUser(user);
        return rows > 0;
    }

    @Override
    @Transactional
    public User login(String loginAccount, String password) {
        User user = null;

        // 1. 依次按 用户名、邮箱、手机号 查询用户
        user = userMapper.selectByUsername(loginAccount);
        if (user == null) {
            user = userMapper.selectByEmail(loginAccount);
        }
        if (user == null) {
            user = userMapper.selectByPhone(loginAccount);
        }

        // 账号不存在
        if (user == null) {
            return null;
        }
        // 账号已被锁定：检查是否已满30分钟
        if (user.getLockTime() != null) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                // 锁定未到期，拒绝登录
                return null;
            }
            // 锁定已到期，自动解锁：清除锁定时间并重置错误次数
            userMapper.updateLockTime(user.getUserId(), null);
            userMapper.updateLoginErrorCount(user.getUserId(), 0);
            user.setLockTime(null);
            user.setLoginErrorCount(0);
        }
        // 账号已禁用
        if (user.getStatus() != 1) {
            return null;
        }

        // 密码校验：明文加密后比对
        String inputEncryptPwd = MD5Util.md5Encrypt(password);
        if (!inputEncryptPwd.equals(user.getPasswordHash())) {
            // 密码错误，错误次数+1
            int newCount = user.getLoginErrorCount() + 1;
            userMapper.updateLoginErrorCount(user.getUserId(), newCount);

            // 达到最大次数，锁定账号
            if (newCount >= MAX_ERROR_COUNT) {
                userMapper.updateLockTime(user.getUserId(), LocalDateTime.now());
            }
            return null;
        }

        // 登录成功：更新最后登录时间 + 重置错误次数
        userMapper.updateLoginSuccessInfo(user.getUserId(), LocalDateTime.now());
        return user;
    }
}
