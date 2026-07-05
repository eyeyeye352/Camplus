package com.camplus.admin.service;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import com.camplus.faq.mappers.FaqMapper;
import com.camplus.vector.mappers.KnowledgeDocMapper;
import com.camplus.vector.mappers.VectorStoreMapper;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.service.VectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataImportService {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);

    @Autowired
    private KnowledgeExtractService extractService;

    @Autowired
    private VectorService vectorService;

    @Autowired
    private FaqMapper faqMapper;

    @Autowired
    private KnowledgeDocMapper knowledgeDocMapper;

    @Autowired
    private VectorStoreMapper vectorStoreMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int CHUNK_SIZE = 500;

    public boolean isModelReady() {
        return vectorService != null && vectorService.embedText("test") != null
                && vectorService.embedText("test").isSuccess();
    }

    public Map<String, Object> importFromRawData() {
        Map<String, Object> result = new HashMap<>();
        int faqCount = 0;
        int docCount = 0;
        int chunkCount = 0;
        int failCount = 0;

        log.info("========================================");
        log.info("开始从 RawData 目录导入数据");
        log.info("========================================");

        try {
            List<KnowledgeExtractDTO> extractedData = extractService.initDataFromRawDataDirectory();
            log.info("文件解析完成，共提取 {} 条数据段", extractedData.size());

            for (KnowledgeExtractDTO dto : extractedData) {
                try {
                    if ("TYPE_FAQ".equals(dto.getDataType())) {
                        faqCount += processFaq(dto);
                    } else if ("TYPE_DOC".equals(dto.getDataType())) {
                        int[] docResult = processDocument(dto);
                        docCount += docResult[0];
                        chunkCount += docResult[1];
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("处理数据段失败: {} - {}", dto.getSourceFileName(), e.getMessage());
                }
            }

            log.info("========================================");
            log.info("数据导入完成！");
            log.info("FAQ导入: {} 条", faqCount);
            log.info("文档导入: {} 个, 分块: {} 条", docCount, chunkCount);
            log.info("失败: {} 条", failCount);
            log.info("========================================");

        } catch (Exception e) {
            log.error("数据导入过程中发生异常: {}", e.getMessage(), e);
        }

        result.put("faqCount", faqCount);
        result.put("docCount", docCount);
        result.put("chunkCount", chunkCount);
        result.put("failCount", failCount);
        return result;
    }

    /**
     * 处理 FAQ 问答数据：
     * 1. 插入 faq_items 表获取自增 faq_id
     * 2. 向量化合并文本
     * 3. 插入 faq_vector_store 表
     */
    private int processFaq(KnowledgeExtractDTO dto) {
        String question = dto.getTitle();
        String answer = dto.getPlainText();
        String combinedText = "问题：" + question + "\n答案：" + answer;

        try {
            VectorEmbeddingResponse response = vectorService.embedText(combinedText);
            if (!response.isSuccess() || response.getDenseVector() == null) {
                log.warn("FAQ向量化失败: {}", question);
                return 0;
            }

            Map<String, Object> faqParams = new HashMap<>();
            faqParams.put("question", question);
            faqParams.put("answer", answer);
            faqParams.put("source", "import");
            faqMapper.insertFaqWithZeroStats(faqParams);
            Object faqIdObj = faqParams.get("faqId");
            if (faqIdObj == null) {
                log.warn("FAQ插入数据库失败，未获取到 faqId: {}", question);
                return 0;
            }
            int faqId = ((Number) faqIdObj).intValue();

            byte[] combinedEmbeddingBytes = floatsToBytes(response.getDenseVector());
            String sparseJson = sparseToJson(response.getSparseVector());

            vectorStoreMapper.insertFaqVector(faqId, question, answer,
                    null, null, combinedEmbeddingBytes, sparseJson);

            log.info("FAQ导入成功 [faqId={}]: {}", faqId, question.length() > 30 ? question.substring(0, 30) + "..." : question);
            return 1;
        } catch (Exception e) {
            log.error("FAQ处理异常: {} - {}", question, e.getMessage());
            return 0;
        }
    }

    /**
     * 处理文档数据：
     * 1. 插入 knowledge_docs 表获取自增 doc_id
     * 2. 文本分块
     * 3. 逐块向量化并插入 knowledge_vector_store 表
     */
    private int[] processDocument(KnowledgeExtractDTO dto) {
        String rawText = dto.getPlainText();
        String sourceFileName = dto.getSourceFileName();
        int insertedChunks = 0;

        try {
            Map<String, Object> docParams = new HashMap<>();
            docParams.put("docName", sourceFileName);
            docParams.put("docPath", "RawData/" + sourceFileName);
            docParams.put("docContent", rawText);
            docParams.put("docType", getFileExtension(sourceFileName));

            int totalChunks = (rawText.length() + CHUNK_SIZE - 1) / CHUNK_SIZE;
            docParams.put("chunkCount", totalChunks);

            knowledgeDocMapper.insertKnowledgeDoc(docParams);
            Object docIdObj = docParams.get("docId");
            if (docIdObj == null) {
                log.warn("文档插入数据库失败，未获取到 docId: {}", sourceFileName);
                return new int[]{0, 0};
            }
            int docId = ((Number) docIdObj).intValue();

            int chunkIndex = 0;
            for (int i = 0; i < rawText.length(); i += CHUNK_SIZE) {
                int end = Math.min(rawText.length(), i + CHUNK_SIZE);
                String chunkContent = rawText.substring(i, end);

                try {
                    VectorEmbeddingResponse response = vectorService.embedText(chunkContent);
                    if (response.isSuccess() && response.getDenseVector() != null) {
                        byte[] chunkEmbeddingBytes = floatsToBytes(response.getDenseVector());
                        String metadata = "{\"source\":\"" + sourceFileName + "\",\"chunk\":" + chunkIndex + "}";
                        String sparseJson = sparseToJson(response.getSparseVector());

                        vectorStoreMapper.insertKnowledgeVector(docId, chunkIndex, chunkContent,
                                chunkEmbeddingBytes, metadata, sparseJson);
                        insertedChunks++;
                    }
                } catch (Exception e) {
                    log.warn("文档分块向量化失败 [docId={}, chunkIndex={}]: {}", docId, chunkIndex, e.getMessage());
                }
                chunkIndex++;
            }

            log.info("文档导入成功 [docId={}]: {} (分块: {}/{})", docId, sourceFileName, insertedChunks, totalChunks);
            return new int[]{1, insertedChunks};
        } catch (Exception e) {
            log.error("文档处理异常: {} - {}", sourceFileName, e.getMessage());
            return new int[]{0, 0};
        }
    }

    private byte[] floatsToBytes(float[] floats) {
        if (floats == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }

    private String sparseToJson(Map<Integer, Float> sparse) {
        if (sparse == null || sparse.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(sparse);
        } catch (Exception e) {
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx < fileName.length() - 1) {
            return fileName.substring(dotIdx + 1).toLowerCase();
        }
        return "unknown";
    }
}
