package com.camplus.login.service.impl;

import com.camplus.login.mappers.UserMapper;
import com.camplus.login.entity.User;
import com.camplus.login.service.UserService;
import com.camplus.login.service.VerificationCodeService;
import com.camplus.login.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

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
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            existUser = userMapper.selectByEmail(user.getEmail());
            if (existUser != null) {
                return null;
            }
        }

        user.setRole(0);

        String encryptPwd = MD5Util.md5Encrypt(user.getPasswordHash());
        user.setPasswordHash(encryptPwd);

        if (user.getUsername() == null || "".equals(user.getUsername())) {
            if (user.getEmail() != null && !"".equals(user.getEmail())) {
                user.setUsername(user.getEmail());
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
        User user = userMapper.selectByUsername(loginAccount);
        if (user == null) {
            user = userMapper.selectByEmail(loginAccount);
        }

        if (user == null) {
            return null;
        }

        String inputEncryptPwd = MD5Util.md5Encrypt(password);
        if (!inputEncryptPwd.equals(user.getPasswordHash())) {
            return null;
        }

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

    @Override
    public boolean isEmailExist(String email) {
        return userMapper.selectByEmail(email) != null;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if ("Administrator".equals(user.getUsername())) {
            throw new IllegalArgumentException("初始管理员账号禁止注销");
        }
        int rows = userMapper.deleteUser(userId);
        return rows > 0;
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        boolean codeValid = verificationCodeService.verifyCode(email, "email", code);
        if (!codeValid) {
            return false;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return false;
        }
        if ("Administrator".equals(user.getUsername())) {
            throw new IllegalArgumentException("初始管理员账号禁止重置密码");
        }
        String encryptedPassword = MD5Util.md5Encrypt(newPassword);
        userMapper.updatePassword(user.getUserId(), encryptedPassword);
        return true;
    }
}
