package com.camplus.problem.entity;

public class FaqProblem {
    // 必须使用 faq_id，而不是 id
    private Long faq_id;
    // 必须使用 category_id，而不是 categoryId
    private Long category_id;
    private String question;
    private String answer;
    private Integer status;
    private Integer view_count; // 必须使用 view_count

    // 更新后的 Getter 和 Setter 方法
    public Long getFaq_id() { return faq_id; }
    public void setFaq_id(Long faq_id) { this.faq_id = faq_id; }

    public Long getCategory_id() { return category_id; }
    public void setCategory_id(Long category_id) { this.category_id = category_id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getView_count() { return view_count; }
    public void setView_count(Integer view_count) { this.view_count = view_count; }
}