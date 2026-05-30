package com.camplus.login.service.serviceImpl;

import com.camplus.login.mappers.UserMapper;
import com.camplus.login.pojo.User;
import com.camplus.login.service.UserService;
import com.camplus.login.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.time.LocalDateTime;

/**
 * 用户业务实现类
 */
public class UserServiceImpl implements UserService {

    // 密码错误最大次数，超过则锁定账号
    private static final int MAX_ERROR_COUNT = 5;

    @Override
    public boolean register(User user) {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);

            // 1. 校验用户名、邮箱、手机号是否已存在
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

            // 2. 初始化新用户默认参数
            user.setRole(0);
            user.setStatus(1);
            user.setLoginErrorCount(0);
            user.setLockTime(null);
            user.setLastLoginTime(null);

            // 3. 插入数据
            int rows = userMapper.insertUser(user);
            return rows > 0;
        }
    }

    @Override
    public User login(String loginAccount, String password) {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
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
            // 账号已被锁定
            if (user.getLockTime() != null) {
                return null;
            }
            // 账号已禁用
            if (user.getStatus() != 1) {
                return null;
            }

            // 密码校验
            if (!password.equals(user.getPasswordHash())) {
                // 密码错误，错误次数+1
                int newCount = user.getLoginErrorCount() + 1;
                userMapper.updateLoginErrorCount(user.getUserId(), newCount);

                // 达到最大次数，锁定账号
                if (newCount >= MAX_ERROR_COUNT) {
                    userMapper.updateLockTime(user.getUserId(), LocalDateTime.now());
                }
                return null;
            }

            // 5. 登录成功：更新最后登录时间 + 重置错误次数
            userMapper.updateLoginSuccessInfo(user.getUserId(), LocalDateTime.now());
            return user;
        }
    }
}