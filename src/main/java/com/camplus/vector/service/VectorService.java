package com.camplus.vector.service;

import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.pojo.VectorSearchResult;

import java.util.List;

public interface VectorService {

    VectorEmbeddingResponse embedText(String text);

    VectorEmbeddingResponse embedTexts(List<String> texts);

    List<VectorSearchResult> search(String tableName, String queryText);

    List<VectorSearchResult> searchWithVector(String tableName, float[] queryVector);
}
