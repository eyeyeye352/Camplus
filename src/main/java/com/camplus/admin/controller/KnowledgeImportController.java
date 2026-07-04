package com.camplus.admin.controller;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import com.camplus.admin.service.KnowledgeExtractService;

// 【关键修复点1】引入组员写好的向量服务和响应实体
import com.camplus.vector.service.VectorService;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
// 【关键修复点2】引入组员写好的 Mapper
import com.camplus.vector.mappers.VectorStoreMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/knowledge")
public class KnowledgeImportController {

    private final KnowledgeExtractService extractService;
    private final VectorService vectorService;
    private final VectorStoreMapper vectorStoreMapper;

    // 构造器注入组员的 VectorService 和 VectorStoreMapper
    public KnowledgeImportController(KnowledgeExtractService extractService,
                                     VectorService vectorService,
                                     VectorStoreMapper vectorStoreMapper) {
        this.extractService = extractService;
        this.vectorService = vectorService;
        this.vectorStoreMapper = vectorStoreMapper;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadAndExtract(@RequestParam("file") MultipartFile file) {
        Map<String, Object> jsonResponse = new HashMap<>();

        if (file.isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "上传的文件是空的");
            return jsonResponse;
        }

        try {
            // 1. 文件落盘并提取为纯文本 DTO
            List<KnowledgeExtractDTO> extractedData = extractService.saveAndExtractUploadedFile(file);

            int successCount = 0;

            // 2. 遍历提取到的数据，进行向量化与入库
            for (KnowledgeExtractDTO dto : extractedData) {
                if ("TYPE_FAQ".equals(dto.getDataType())) {
                    successCount += processFaqData(dto);
                } else if ("TYPE_DOC".equals(dto.getDataType())) {
                    successCount += processDocumentData(dto);
                }
            }

            jsonResponse.put("success", true);
            jsonResponse.put("msg", "文件处理完成！共提取数据段: " + extractedData.size() + "，成功向量化入库: " + successCount);

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "文件处理异常: " + e.getMessage());
        }

        return jsonResponse;
    }

    /**
     * 处理结构化 FAQ 数据：合并问题和答案 -> 向量化 -> 入库 faq_vector_store
     */
    private int processFaqData(KnowledgeExtractDTO dto) {
        String combinedText = "问题：" + dto.getTitle() + "\n答案：" + dto.getPlainText();

        try {
            // 调用组员的本地 ONNX 模型服务进行向量化
            VectorEmbeddingResponse response = vectorService.embedText(combinedText);
            if (response.isSuccess() && response.getDenseVector() != null) {

                // 【核心修复】将 float[] 转为 byte[] 以适配组员的 MySQL BLOB 设计
                byte[] combinedEmbeddingBytes = floatsToBytes(response.getDenseVector());

                // 组员的 Mapper 要求 faqId 是 Integer
                Integer faqId = Math.abs(UUID.randomUUID().hashCode());

                // 稀疏向量 JSON
                String sparseJson = null;
                if (response.getSparseVector() != null) {
                    sparseJson = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(response.getSparseVector());
                }

                // 注意：组员的接口支持单独传问题和答案的向量，为了节省计算资源，这里我们仅存 combined 向量，其余传 null
                vectorStoreMapper.insertFaqVector(faqId, dto.getTitle(), dto.getPlainText(),
                        null, null, combinedEmbeddingBytes, sparseJson);
                return 1;
            }
        } catch (Exception e) {
            System.err.println("FAQ向量化失败：" + dto.getTitle());
        }
        return 0;
    }

    /**
     * 处理非结构化长文档：分块 (Chunking) -> 遍历向量化 -> 入库 knowledge_vector_store
     */
    private int processDocumentData(KnowledgeExtractDTO dto) {
        int insertedChunks = 0;
        String rawText = dto.getPlainText();
        String sourceFileName = dto.getSourceFileName();

        // 组员的 Mapper 要求 docId 是 Integer
        Integer docId = Math.abs(UUID.randomUUID().hashCode());

        // 固定长度切片算法
        int chunkSize = 500;
        int chunkIndex = 0;

        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        for (int i = 0; i < rawText.length(); i += chunkSize) {
            int end = Math.min(rawText.length(), i + chunkSize);
            String chunkContent = rawText.substring(i, end);

            try {
                VectorEmbeddingResponse response = vectorService.embedText(chunkContent);
                if (response.isSuccess() && response.getDenseVector() != null) {

                    // 【核心修复】将 float[] 转为 byte[]
                    byte[] chunkEmbeddingBytes = floatsToBytes(response.getDenseVector());
                    String metadata = "{\"source\":\"" + sourceFileName + "\"}";

                    // 稀疏向量 JSON
                    String sparseJson = null;
                    if (response.getSparseVector() != null) {
                        sparseJson = objectMapper.writeValueAsString(response.getSparseVector());
                    }

                    vectorStoreMapper.insertKnowledgeVector(docId, chunkIndex, chunkContent, chunkEmbeddingBytes, metadata, sparseJson);
                    insertedChunks++;
                }
            } catch (Exception e) {
                System.err.println("文档分块向量化失败，分块索引：" + chunkIndex);
            }
            chunkIndex++;
        }
        return insertedChunks;
    }

    /**
     * 【底层桥接引擎】将 BGE-M3 模型生成的 float[] 转换为 MySQL BLOB 需要的 byte[]
     */
    private byte[] floatsToBytes(float[] floats) {
        if (floats == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        // 使用高效的块写入
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }
}