package com.camplus.vector.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.onnxruntime.*;
import dev.langchain4j.data.embedding.Embedding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.file.Paths;
import java.util.*;

@Service
public class BgeM3OnnxService {

    private static final Logger log = LoggerFactory.getLogger(BgeM3OnnxService.class);

    @Value("${bge-m3.model-path:D:/models/bge-m3-onnx/}")
    private String modelPath;

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;
    private final ObjectMapper mapper = new ObjectMapper();

    private boolean initialized = false;
    private String initErrorMessage = null;

    @Value("${bge-m3.dense-weight:0.6}")
    private float denseWeight;

    @Value("${bge-m3.sparse-weight:0.4}")
    private float sparseWeight;

    private static BgeM3OnnxService instance;

    public static BgeM3OnnxService getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() {
        instance = this;
        try {
            env = OrtEnvironment.getEnvironment();

            OrtSession.SessionOptions options = new OrtSession.SessionOptions();

            String modelFilePath = modelPath + "model.onnx";
            log.info("正在加载BGE-M3 ONNX模型: {}", modelFilePath);
            session = env.createSession(modelFilePath, options);

            tokenizer = HuggingFaceTokenizer.newInstance(
                Paths.get(modelPath + "tokenizer.json"),
                Map.of(
                    "addSpecialTokens", "true",
                    "truncation", "true",
                    "maxLength", "8192",
                    "padding", "false"
                )
            );

            initialized = true;
            log.info("BGE-M3 ONNX版本加载成功");
            log.info("模型输入: {}", session.getInputNames());
            log.info("模型输出: {}", session.getOutputNames());
        } catch (Exception e) {
            initialized = false;
            initErrorMessage = "BGE-M3 ONNX模型加载失败: " + e.getMessage();
            log.error(initErrorMessage);
            log.error("请检查模型文件路径是否正确: {}", modelPath);
            log.error("应用将继续启动，但向量检索功能将不可用");
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            log.info("BGE-M3 ONNX模型资源已释放");
        } catch (Exception e) {
            log.error("关闭BGE-M3 ONNX模型资源时出错: {}", e.getMessage());
        }
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("BGE-M3 ONNX模型未初始化。" + 
                (initErrorMessage != null ? initErrorMessage : "请检查模型文件路径配置。"));
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getInitErrorMessage() {
        return initErrorMessage;
    }

    public BgeM3Result encode(String text) {
        checkInitialized();
        try {
            Encoding encoding = tokenizer.encode(text);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(env, new long[][]{inputIds}));
            inputs.put("attention_mask", OnnxTensor.createTensor(env, new long[][]{attentionMask}));

            try (OrtSession.Result result = session.run(inputs)) {
                
                float[][] denseVec = (float[][]) result.get(0).getValue();
                Map<Integer, Float> sparseVec = new HashMap<>();
                
                if (result.size() >= 2) {
                    try {
                        float[][][] tokenWeights = (float[][][]) result.get(1).getValue();
                        
                        Set<Integer> unusedTokens = Set.of(0, 1, 2, 3);
                        
                        int actualSeqLen = Math.min(inputIds.length, tokenWeights[0].length);
                        
                        for (int i = 0; i < actualSeqLen; i++) {
                            long tokenId = inputIds[i];
                            float weight = tokenWeights[0][i][0];
                            
                            if (!unusedTokens.contains((int)tokenId) && weight > 0) {
                                int tid = (int)tokenId;
                                if (!sparseVec.containsKey(tid) || weight > sparseVec.get(tid)) {
                                    sparseVec.put(tid, weight);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("提取稀疏向量失败: {}", e.getMessage(), e);
                    }
                }

                return new BgeM3Result(denseVec[0], sparseVec);
            }
        } catch (Exception e) {
            log.error("文本编码失败: {}", e.getMessage(), e);
            throw new RuntimeException("文本编码失败", e);
        }
    }

    public Embedding embed(String text) {
        BgeM3Result result = encode(text);
        return Embedding.from(result.dense);
    }

    public float cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0f;
        }
        float dot = 0.0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot;
    }

    public float lexicalSimilarity(Map<Integer, Float> querySparse, Map<Integer, Float> docSparse) {
        if (querySparse == null || docSparse == null || querySparse.isEmpty() || docSparse.isEmpty()) {
            return 0.0f;
        }
        float score = 0.0f;
        for (Map.Entry<Integer, Float> entry : querySparse.entrySet()) {
            int tokenId = entry.getKey();
            float queryWeight = entry.getValue();
            if (docSparse.containsKey(tokenId)) {
                score += queryWeight * docSparse.get(tokenId);
            }
        }
        return score;
    }

    public float hybridSimilarity(BgeM3Result query, BgeM3Result doc) {
        float denseScore = cosineSimilarity(query.dense, doc.dense);
        float sparseScore = lexicalSimilarity(query.sparse, doc.sparse);
        return denseWeight * denseScore + sparseWeight * sparseScore;
    }

    public float hybridSimilarity(float[] denseA, Map<Integer, Float> sparseA,
                                  float[] denseB, Map<Integer, Float> sparseB) {
        float denseScore = cosineSimilarity(denseA, denseB);
        float sparseScore = lexicalSimilarity(sparseA, sparseB);
        return denseWeight * denseScore + sparseWeight * sparseScore;
    }

    public static class BgeM3Result {
        public final float[] dense;
        public final Map<Integer, Float> sparse;

        public BgeM3Result(float[] dense, Map<Integer, Float> sparse) {
            this.dense = dense;
            this.sparse = sparse;
        }

        public String toSparseJson(ObjectMapper mapper) {
            try {
                return mapper.writeValueAsString(sparse);
            } catch (Exception e) {
                return "{}";
            }
        }
    }
}
