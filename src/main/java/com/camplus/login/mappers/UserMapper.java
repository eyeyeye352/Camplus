package com.camplus.login.mappers;

import com.camplus.login.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    // 注册新增用户
    int insertUser(User user);

    // 登录校验
    User selectByUsername(String username);
    User selectByPhone(String phone);
    User selectByEmail(String email);

    // 登录失败时更新错误次数
    int updateLoginErrorCount(@Param("userId") Long userId, @Param("errorCount") Integer errorCount);

    // 更新账号锁定时间
    int updateLockTime(@Param("userId") Long userId, @Param("lockTime") java.time.LocalDateTime lockTime);

    // 更新最后登录时间并重置错误次数
    int updateLoginSuccessInfo(@Param("userId") Long userId, @Param("loginTime") java.time.LocalDateTime loginTime);

    // 更新用户名
    int updateUsername(@Param("userId") Long userId, @Param("username") String username);

    // 更新邮箱
    int updateEmail(@Param("userId") Long userId, @Param("email") String email);

    // 更新手机号
    int updatePhone(@Param("userId") Long userId, @Param("phone") String phone);

    // 更新密码
    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    // 根据用户ID查询
    User selectById(@Param("userId") Long userId);
}