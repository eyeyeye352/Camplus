package com.camplus.problem.entity;

public class FaqProblem {
    private Long id;          // 问题唯一ID
    private Long categoryId;  // 所属分类ID
    private String question;  // 问题内容
    private String answer;    // 问题答案
    private Integer status;   // 状态 (1:正常, 0:隐藏)[cite: 2]
    private Integer viewCount;// 浏览次数[cite: 2]

    // 更新后的 Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
}