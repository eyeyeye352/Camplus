package com.camplus.admin.pojo;

public class ReviewRequestDTO {
    private Long contributionId;
    private Integer status; // 1: 通过, 2: 拒绝
    private String comment; // 拒绝理由或审核备注

    // 👇 如果管理员选择了“修改后通过”，前端需要把修改后的内容传过来；
    // 如果是“直接通过”，前端直接把原内容原封不动传过来即可。
    private String finalQuestion;
    private String finalAnswer;
    private String finalContent;
    private String finalSourceUrl;
    private Long categoryId; // 入库时需要知道分到哪个板块（前端下拉框选择）

    public Long getContributionId() {
        return contributionId;
    }

    public void setContributionId(Long contributionId) {
        this.contributionId = contributionId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFinalQuestion() {
        return finalQuestion;
    }

    public void setFinalQuestion(String finalQuestion) {
        this.finalQuestion = finalQuestion;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public void setFinalAnswer(String finalAnswer) {
        this.finalAnswer = finalAnswer;
    }

    public String getFinalContent() {
        return finalContent;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }

    public String getFinalSourceUrl() {
        return finalSourceUrl;
    }

    public void setFinalSourceUrl(String finalSourceUrl) {
        this.finalSourceUrl = finalSourceUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
// 省略 Getter 和 Setter 方法...
}
