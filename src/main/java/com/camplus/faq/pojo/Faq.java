package com.camplus.faq.pojo;

import java.time.LocalDateTime;

public class Faq {
    private Integer faqId;
    private Integer questionCount;
    private Integer likeCount;
    private Integer hotScore;
    private Integer displayStatus;
    private LocalDateTime createTime;
    private String question;
    private String answer;

    public Faq() {}

    public Faq(Integer faqId, Integer questionCount, Integer likeCount, Integer hotScore, Integer displayStatus, LocalDateTime createTime, String question, String answer) {
        this.faqId = faqId;
        this.questionCount = questionCount;
        this.likeCount = likeCount;
        this.hotScore = hotScore;
        this.displayStatus = displayStatus;
        this.createTime = createTime;
        this.question = question;
        this.answer = answer;
    }

    public Integer getFaqId() {
        return faqId;
    }

    public void setFaqId(Integer faqId) {
        this.faqId = faqId;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getHotScore() {
        return hotScore;
    }

    public void setHotScore(Integer hotScore) {
        this.hotScore = hotScore;
    }

    public Integer getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(Integer displayStatus) {
        this.displayStatus = displayStatus;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
