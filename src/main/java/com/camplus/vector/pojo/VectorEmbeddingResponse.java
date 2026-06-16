package com.camplus.vector.pojo;

import java.util.Map;

public class VectorEmbeddingResponse {
    private boolean success;
    private float[] denseVector;
    private Map<Integer, Float> sparseVector;
    private String text;
    private String message;

    public VectorEmbeddingResponse() {}

    public static VectorEmbeddingResponse success(float[] dense, Map<Integer, Float> sparse, String text) {
        VectorEmbeddingResponse response = new VectorEmbeddingResponse();
        response.setSuccess(true);
        response.setDenseVector(dense);
        response.setSparseVector(sparse);
        response.setText(text);
        return response;
    }

    public static VectorEmbeddingResponse failure(String message) {
        VectorEmbeddingResponse response = new VectorEmbeddingResponse();
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

    public float[] getDenseVector() {
        return denseVector;
    }

    public void setDenseVector(float[] denseVector) {
        this.denseVector = denseVector;
    }

    public Map<Integer, Float> getSparseVector() {
        return sparseVector;
    }

    public void setSparseVector(Map<Integer, Float> sparseVector) {
        this.sparseVector = sparseVector;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
