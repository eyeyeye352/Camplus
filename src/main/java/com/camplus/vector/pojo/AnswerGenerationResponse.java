package com.camplus.vector.pojo;

import java.util.List;

public class AnswerGenerationResponse {
    private boolean success;
    private String answer;
    private List<String> contextsUsed;
    private float confidence;
    private String message;

    public AnswerGenerationResponse() {}

    public static AnswerGenerationResponse success(String answer, List<String> contexts, float confidence) {
        AnswerGenerationResponse response = new AnswerGenerationResponse();
        response.setSuccess(true);
        response.setAnswer(answer);
        response.setContextsUsed(contexts);
        response.setConfidence(confidence);
        return response;
    }

    public static AnswerGenerationResponse failure(String message) {
        AnswerGenerationResponse response = new AnswerGenerationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getContextsUsed() {
        return contextsUsed;
    }

    public void setContextsUsed(List<String> contextsUsed) {
        this.contextsUsed = contextsUsed;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
