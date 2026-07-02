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

    private static final int MAX_ERROR_COUNT = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public boolean register(User user) {
        return registerAndReturnUser(user) != null;
    }

    @Override
    @Transactional
    public User registerAndReturnUser(User user) {
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            return null;
        }
        existUser = userMapper.selectByEmail(user.getEmail());
        if (existUser != null) {
            return null;
        }
        existUser = userMapper.selectByPhone(user.getPhone());
        if (existUser != null) {
            return null;
        }

        user.setRole(0);
        user.setStatus(1);
        user.setLoginErrorCount(0);
        user.setLockTime(null);
        user.setLastLoginTime(null);

        String encryptPwd = MD5Util.md5Encrypt(user.getPasswordHash());
        user.setPasswordHash(encryptPwd);

        if (user.getUsername() == null || "".equals(user.getUsername())) {
            if (user.getEmail() != null && !"".equals(user.getEmail())) {
                user.setUsername(user.getEmail());
            } else if (user.getPhone() != null && !"".equals(user.getPhone())) {
                user.setUsername(user.getPhone());
            }
        }

        int rows = userMapper.insertUser(user);
        if (rows > 0) {
            return userMapper.selectByUsername(user.getUsername());
        }
        return null;
    }

    @Override
    @Transactional
    public User login(String loginAccount, String password) {
        User user = null;

        user = userMapper.selectByUsername(loginAccount);
        if (user == null) {
            user = userMapper.selectByEmail(loginAccount);
        }
        if (user == null) {
            user = userMapper.selectByPhone(loginAccount);
        }

        if (user == null) {
            return null;
        }
        if (user.getLockTime() != null) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                return null;
            }
            userMapper.updateLockTime(user.getUserId(), null);
            userMapper.updateLoginErrorCount(user.getUserId(), 0);
            user.setLockTime(null);
            user.setLoginErrorCount(0);
        }
        if (user.getStatus() != 1) {
            return null;
        }

        String inputEncryptPwd = MD5Util.md5Encrypt(password);
        if (!inputEncryptPwd.equals(user.getPasswordHash())) {
            int newCount = user.getLoginErrorCount() + 1;
            userMapper.updateLoginErrorCount(user.getUserId(), newCount);

            if (newCount >= MAX_ERROR_COUNT) {
                userMapper.updateLockTime(user.getUserId(), LocalDateTime.now());
            }
            return null;
        }

        userMapper.updateLoginSuccessInfo(user.getUserId(), LocalDateTime.now());
        return user;
    }

    @Override
    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        User exist = userMapper.selectByUsername(newUsername);
        if (exist != null && !exist.getUserId().equals(userId)) {
            return null;
        }
        int rows = userMapper.updateUsername(userId, newUsername);
        if (rows > 0) {
            return userMapper.selectById(userId);
        }
        return null;
    }

    @Override
    @Transactional
    public User updateEmail(Long userId, String newEmail) {
        User exist = userMapper.selectByEmail(newEmail);
        if (exist != null && !exist.getUserId().equals(userId)) {
            return null;
        }
        int rows = userMapper.updateEmail(userId, newEmail);
        if (rows > 0) {
            return userMapper.selectById(userId);
        }
        return null;
    }

    @Override
    @Transactional
    public User updatePhone(Long userId, String newPhone) {
        User exist = userMapper.selectByPhone(newPhone);
        if (exist != null && !exist.getUserId().equals(userId)) {
            return null;
        }
        int rows = userMapper.updatePhone(userId, newPhone);
        if (rows > 0) {
            return userMapper.selectById(userId);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        String oldEncrypt = MD5Util.md5Encrypt(oldPassword);
        if (!oldEncrypt.equals(user.getPasswordHash())) {
            return false;
        }
        String newEncrypt = MD5Util.md5Encrypt(newPassword);
        int rows = userMapper.updatePassword(userId, newEncrypt);
        return rows > 0;
    }

    @Override
    public Integer getRole(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getRole() : null;
    }
}
