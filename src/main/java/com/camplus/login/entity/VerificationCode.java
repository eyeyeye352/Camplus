package com.camplus.login.entity;

import java.time.LocalDateTime;

public class VerificationCode {

    private Long id;
    private String target;
    private String code;
    private String type;
    private Integer status;
    private Integer errorCount;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public VerificationCode() {}

    public VerificationCode(Long id, String target, String code, String type, Integer status, 
                           Integer errorCount, LocalDateTime expireTime, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.target = target;
        this.code = code;
        this.type = type;
        this.status = status;
        this.errorCount = errorCount;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
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
        return "VerificationCode{" +
                "id=" + id +
                ", target='" + target + '\'' +
                ", code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", status=" + status +
                ", errorCount=" + errorCount +
                ", expireTime=" + expireTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}