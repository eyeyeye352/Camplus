package com.camplus.vector.pojo;

public class VectorSearchResult {
    private Long recordId;
    private String content;
    private float similarityScore;
    private String metadata;
    private String tableName;

    public VectorSearchResult() {}

    public VectorSearchResult(Long recordId, String content, float similarityScore, String metadata, String tableName) {
        this.recordId = recordId;
        this.content = content;
        this.similarityScore = similarityScore;
        this.metadata = metadata;
        this.tableName = tableName;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(float similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
