package com.camplus.admin.pojo;

import java.time.LocalDateTime;

public class UserContribution {
    private Integer contributionId;
    private Integer userId;
    private Integer contributionType;
    private String title;
    private String content;
    private Integer status;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getContributionId() {
        return contributionId;
    }

    public void setContributionId(Integer contributionId) {
        this.contributionId = contributionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getContributionType() {
        return contributionType;
    }

    public void setContributionType(Integer contributionType) {
        this.contributionType = contributionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
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

    public UserContribution() {}

    // 省略 Getter 和 Setter 方法，请使用 IDE 自动生成...
}