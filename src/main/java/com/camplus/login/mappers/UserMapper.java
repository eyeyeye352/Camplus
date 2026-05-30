package com.camplus.login.mappers;

import com.camplus.login.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 1. 新增用户（注册）
     */
    int insertUser(User user);

    /**
     * 2. 根据用户名查询用户（用户名登录校验）
     */
    User selectByUsername(String username);

    /**
     * 3. 根据手机号查询用户（登录校验）
     */
    User selectByPhone(String phone);

    /**
     * 4. 根据邮箱查询用户（登录校验）
     */
    User selectByEmail(String email);

    /**
     * 5. 更新登录错误次数（登录失败时调用）
     */
    int updateLoginErrorCount(Integer userId, Integer errorCount);

    /**
     * 6. 更新账号锁定时间（锁定账号时调用）
     */
    int updateLockTime(Integer userId, java.time.LocalDateTime lockTime);

    /**
     * 7. 更新最后登录时间并重置错误次数（登录成功时调用）
     */
    int updateLoginSuccessInfo(Integer userId, java.time.LocalDateTime loginTime);
}