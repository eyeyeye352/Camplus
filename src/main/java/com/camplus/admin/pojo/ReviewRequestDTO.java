package com.camplus.admin.pojo;

public class ReviewRequestDTO {
    private Long contributionId;
    private Integer status; // 1: 通过, 2: 拒绝
    private String comment; // 拒绝理由或审核备注

    // 👇 如果管理员选择了“修改后通过”，前端需要把修改后的内容传过来；
    // 如果是“直接通过”，前端直接把原内容原封不动传过来即可。
    private String finalQuestion;
    private String finalAnswer;

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

}
