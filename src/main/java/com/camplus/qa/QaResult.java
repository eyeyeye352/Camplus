package com.camplus.qa;

public class QaResult {
    private String answer;
    private boolean faqHit;

    public QaResult(String answer, boolean faqHit) {
        this.answer = answer;
        this.faqHit = faqHit;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isFaqHit() {
        return faqHit;
    }
}