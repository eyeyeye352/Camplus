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

    /**
     * 修改用户名
     * @param userId 用户ID
     * @param newUsername 新用户名
     * @return 修改成功返回更新后的用户对象，失败返回null
     */
    User updateUsername(Long userId, String newUsername);

    /**
     * 修改邮箱
     * @param userId 用户ID
     * @param newEmail 新邮箱
     * @return 修改成功返回更新后的用户对象，失败返回null
     */
    User updateEmail(Long userId, String newEmail);

    /**
     * 修改手机号
     * @param userId 用户ID
     * @param newPhone 新手机号
     * @return 修改成功返回更新后的用户对象，失败返回null
     */
    User updatePhone(Long userId, String newPhone);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 修改成功返回true，失败返回false
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 查询用户角色
     * @param userId 用户ID
     * @return 用户角色（0=普通用户，1=管理员），未找到返回null
     */
    Integer getRole(Long userId);
}