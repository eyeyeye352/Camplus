package com.camplus.login.entity;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 * 对应数据库 users 表
 */
public class User {

    // 主键ID，自增
    private Long userId;

    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String phone;

    // 头像URL
    private String avatarUrl;

    // 用户角色（0:普通用户, 1:管理员）
    private Integer role;

    // 账号状态（0:禁用, 1:正常）
    private Integer status;

    // 最后登录时间
    private LocalDateTime lastLoginTime;

    // 登录错误次数（用于锁定账号）
    private Integer loginErrorCount;

    // 账号锁定时间
    private LocalDateTime lockTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;


    public User() {}

    // 方便创建对象用
    public User(Long userId, String username, String passwordHash, String nickname, String email, String phone, String avatarUrl, Integer role, Integer status, LocalDateTime lastLoginTime, Integer loginErrorCount, LocalDateTime lockTime, LocalDateTime createTime, LocalDateTime updateTime) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
        this.loginErrorCount = loginErrorCount;
        this.lockTime = lockTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getter 和 Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getLoginErrorCount() {
        return loginErrorCount;
    }

    public void setLoginErrorCount(Integer loginErrorCount) {
        this.loginErrorCount = loginErrorCount;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", lastLoginTime=" + lastLoginTime +
                ", loginErrorCount=" + loginErrorCount +
                ", lockTime=" + lockTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}