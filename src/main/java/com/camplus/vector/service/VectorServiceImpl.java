package com.camplus.vector.service;

import com.camplus.vector.mappers.VectorStoreMapper;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.pojo.VectorSearchResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VectorServiceImpl implements VectorService {

    private static final Logger log = LoggerFactory.getLogger(VectorServiceImpl.class);

    private static final int FIXED_TOP_K = 1;
    private static final float FIXED_MIN_SCORE = 0.6f;

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @Autowired
    private VectorStoreMapper vectorStoreMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VectorEmbeddingResponse embedText(String text) {
        try {
            if (!bgeM3OnnxService.isInitialized()) {
                return VectorEmbeddingResponse.failure(bgeM3OnnxService.getInitErrorMessage());
            }

            BgeM3OnnxService.BgeM3Result result = bgeM3OnnxService.encode(text);
            return VectorEmbeddingResponse.success(
                result.dense,
                result.sparse,
                text
            );
        } catch (Exception e) {
            log.error("文本向量化失败: {}", e.getMessage(), e);
            return VectorEmbeddingResponse.failure("文本向量化失败: " + e.getMessage());
        }
    }

    @Override
    public VectorEmbeddingResponse embedTexts(List<String> texts) {
        try {
            if (!bgeM3OnnxService.isInitialized()) {
                return VectorEmbeddingResponse.failure(bgeM3OnnxService.getInitErrorMessage());
            }

            if (texts == null || texts.isEmpty()) {
                return VectorEmbeddingResponse.failure("输入文本列表为空");
            }

            BgeM3OnnxService.BgeM3Result result = bgeM3OnnxService.encode(texts.get(0));
            return VectorEmbeddingResponse.success(
                result.dense,
                result.sparse,
                texts.get(0)
            );
        } catch (Exception e) {
            log.error("批量文本向量化失败: {}", e.getMessage(), e);
            return VectorEmbeddingResponse.failure("批量文本向量化失败: " + e.getMessage());
        }
    }

    @Override
    public List<VectorSearchResult> search(String tableName, String queryText) {
        return search(tableName, queryText, FIXED_MIN_SCORE, FIXED_TOP_K);
    }

    @Override
    public List<VectorSearchResult> search(String tableName, String queryText, float minScore, int topK) {
        return search(tableName, queryText, minScore, topK, 0.6f, 0.4f);
    }

    @Override
    public List<VectorSearchResult> search(String tableName, String queryText, float minScore, int topK,
                                           float denseWeight, float sparseWeight) {
        try {
            if (!bgeM3OnnxService.isInitialized()) {
                log.error("BGE-M3模型未初始化");
                return Collections.emptyList();
            }

            validateTableName(tableName);

            BgeM3OnnxService.BgeM3Result queryResult = bgeM3OnnxService.encode(queryText);
            float[] queryDense = queryResult.dense;
            Map<Integer, Float> querySparse = queryResult.sparse;

            List<Map<String, Object>> vectors = loadVectorsFromTable(tableName);
            log.info("[向量检索] 表={}, 已加载{}条向量, 阈值={}, Top={}, dense={}, sparse={}, 查询=\"{}\"",
                    tableName, vectors.size(), minScore, topK, denseWeight, sparseWeight,
                    queryText.length() > 50 ? queryText.substring(0, 50) + "..." : queryText);

            List<VectorSearchResultWithScore> results = new ArrayList<>();

            float bestScore = -1f;
            String bestName = null;
            Long bestId = null;

            for (Map<String, Object> vectorMap : vectors) {
                try {
                    float[] storedDense = parseFloatArrayFromBytes((byte[]) vectorMap.get("combined_embedding"));
                    if (storedDense == null) {
                        storedDense = parseFloatArrayFromBytes((byte[]) vectorMap.get("chunk_embedding"));
                    }

                    String sparseJson = null;
                    if (vectorMap.containsKey("sparse_embedding")) {
                        sparseJson = (String) vectorMap.get("sparse_embedding");
                    }

                    Map<Integer, Float> storedSparse = new HashMap<>();
                    if (sparseJson != null && !sparseJson.isEmpty()) {
                        try {
                            storedSparse = objectMapper.readValue(sparseJson, new TypeReference<Map<Integer, Float>>() {});
                        } catch (Exception e) {
                            log.warn("解析稀疏向量失败: {}", e.getMessage());
                        }
                    }

                    float denseScore = bgeM3OnnxService.cosineSimilarity(queryDense, storedDense);
                    float sparseScore = bgeM3OnnxService.lexicalSimilarity(querySparse, storedSparse);
                    float score = denseWeight * denseScore + sparseWeight * sparseScore;

                    String name = null;
                    if (vectorMap.containsKey("question")) {
                        name = (String) vectorMap.get("question");
                    } else if (vectorMap.containsKey("doc_name")) {
                        name = (String) vectorMap.get("doc_name");
                    }
                    Long recordId = ((Number) vectorMap.get("id")).longValue();
                    if (vectorMap.containsKey("faq_id")) {
                        recordId = ((Number) vectorMap.get("faq_id")).longValue();
                    } else if (vectorMap.containsKey("doc_id")) {
                        recordId = ((Number) vectorMap.get("doc_id")).longValue();
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestName = name;
                        bestId = recordId;
                    }

                    if (score >= minScore) {
                        String content = "";
                        if (vectorMap.containsKey("question")) {
                            content = (String) vectorMap.get("question");
                            if (vectorMap.containsKey("answer")) {
                                content += "\n" + vectorMap.get("answer");
                            }
                        } else if (vectorMap.containsKey("chunk_content")) {
                            content = (String) vectorMap.get("chunk_content");
                        }

                        results.add(new VectorSearchResultWithScore(
                            recordId, content, score, null, tableName
                        ));
                    }
                } catch (Exception e) {
                    log.warn("处理向量记录失败: {}", e.getMessage());
                }
            }

            List<VectorSearchResultWithScore> sorted = results.stream()
                .sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .collect(Collectors.toList());

            if (sorted.isEmpty()) {
                if (bestName != null) {
                    String preview = bestName.replace("\n", " ");
                    if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
                    log.info("[向量检索] 表={} 未达阈值({}), 最高相似度: id={} score={} 名称=\"{}\"",
                            tableName, minScore, bestId, String.format("%.4f", bestScore), preview);
                } else {
                    log.info("[向量检索] 表={} 无数据", tableName);
                }
            } else {
                for (VectorSearchResultWithScore r : sorted) {
                    String preview = r.getContent().replace("\n", " ");
                    if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
                    log.info("[向量检索] 表={} 命中 id={} score={} 内容=\"{}\"",
                            tableName, r.getRecordId(), String.format("%.4f", r.getScore()), preview);
                }
            }

            return sorted.stream()
                .map(r -> new VectorSearchResult(
                    r.getRecordId(), r.getContent(), r.getScore(), r.getMetadata(), r.getTableName()
                ))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<VectorSearchResult> searchWithVector(String tableName, float[] queryVector) {
        try {
            validateTableName(tableName);

            List<Map<String, Object>> vectors = loadVectorsFromTable(tableName);

            List<VectorSearchResultWithScore> results = new ArrayList<>();

            for (Map<String, Object> vectorMap : vectors) {
                try {
                    float[] storedDense = parseFloatArrayFromBytes((byte[]) vectorMap.get("combined_embedding"));
                    if (storedDense == null) {
                        storedDense = parseFloatArrayFromBytes((byte[]) vectorMap.get("chunk_embedding"));
                    }

                    if (storedDense == null) continue;

                    float score = bgeM3OnnxService.cosineSimilarity(queryVector, storedDense);

                    if (score >= FIXED_MIN_SCORE) {
                        Long recordId = ((Number) vectorMap.get("id")).longValue();
                        if (vectorMap.containsKey("faq_id")) {
                            recordId = ((Number) vectorMap.get("faq_id")).longValue();
                        } else if (vectorMap.containsKey("doc_id")) {
                            recordId = ((Number) vectorMap.get("doc_id")).longValue();
                        }

                        String content = "";
                        if (vectorMap.containsKey("question")) {
                            content = (String) vectorMap.get("question");
                            if (vectorMap.containsKey("answer")) {
                                content += "\n" + vectorMap.get("answer");
                            }
                        } else if (vectorMap.containsKey("chunk_content")) {
                            content = (String) vectorMap.get("chunk_content");
                        }

                        results.add(new VectorSearchResultWithScore(
                            recordId, content, score, null, tableName
                        ));
                    }
                } catch (Exception e) {
                    log.warn("处理向量记录失败: {}", e.getMessage());
                }
            }

            return results.stream()
                .sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
                .limit(FIXED_TOP_K)
                .map(r -> new VectorSearchResult(
                    r.getRecordId(), r.getContent(), r.getScore(), r.getMetadata(), r.getTableName()
                ))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void validateTableName(String tableName) {
        if (!"faq_vector_store".equals(tableName) && !"knowledge_vector_store".equals(tableName)) {
            throw new IllegalArgumentException("不支持的表名: " + tableName +
                "，仅支持 faq_vector_store 和 knowledge_vector_store");
        }
    }

    private List<Map<String, Object>> loadVectorsFromTable(String tableName) {
        if ("faq_vector_store".equals(tableName)) {
            return vectorStoreMapper.searchFaqVectors();
        } else if ("knowledge_vector_store".equals(tableName)) {
            return vectorStoreMapper.searchKnowledgeVectors();
        }
        return Collections.emptyList();
    }

    private float[] parseFloatArrayFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] result = new float[bytes.length / 4];
        buffer.asFloatBuffer().get(result);
        return result;
    }

    private static class VectorSearchResultWithScore {
        private final Long recordId;
        private final String content;
        private final float score;
        private final String metadata;
        private final String tableName;

        public VectorSearchResultWithScore(Long recordId, String content, float score, String metadata, String tableName) {
            this.recordId = recordId;
            this.content = content;
            this.score = score;
            this.metadata = metadata;
            this.tableName = tableName;
        }

        public Long getRecordId() { return recordId; }
        public String getContent() { return content; }
        public float getScore() { return score; }
        public String getMetadata() { return metadata; }
        public String getTableName() { return tableName; }
    }
}
