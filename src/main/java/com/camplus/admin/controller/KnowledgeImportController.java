package com.camplus.admin.controller;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import com.camplus.admin.service.KnowledgeExtractService;

import com.camplus.vector.service.VectorService;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.mappers.VectorStoreMapper;
import com.camplus.vector.mappers.KnowledgeDocMapper;
import com.camplus.faq.mappers.FaqMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/knowledge")
public class KnowledgeImportController {

    private final KnowledgeExtractService extractService;
    private final VectorService vectorService;
    private final VectorStoreMapper vectorStoreMapper;
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final FaqMapper faqMapper;

    public KnowledgeImportController(KnowledgeExtractService extractService,
                                     VectorService vectorService,
                                     VectorStoreMapper vectorStoreMapper,
                                     KnowledgeDocMapper knowledgeDocMapper,
                                     FaqMapper faqMapper) {
        this.extractService = extractService;
        this.vectorService = vectorService;
        this.vectorStoreMapper = vectorStoreMapper;
        this.knowledgeDocMapper = knowledgeDocMapper;
        this.faqMapper = faqMapper;
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
        String question = dto.getTitle();
        String answer = dto.getPlainText();
        String combinedText = "问题：" + question + "\n答案：" + answer;

        try {
            VectorEmbeddingResponse response = vectorService.embedText(combinedText);
            if (!response.isSuccess() || response.getDenseVector() == null) {
                System.err.println("FAQ向量化失败：" + question);
                return 0;
            }

            Map<String, Object> faqParams = new HashMap<>();
            faqParams.put("question", question);
            faqParams.put("answer", answer);
            faqParams.put("source", "manual");
            faqMapper.insertFaq(faqParams);

            Object faqIdObj = faqParams.get("faqId");
            if (faqIdObj == null) {
                System.err.println("FAQ插入数据库失败，未获取到 faqId: " + question);
                return 0;
            }
            int faqId = ((Number) faqIdObj).intValue();

            byte[] combinedEmbeddingBytes = floatsToBytes(response.getDenseVector());

            String sparseJson = null;
            if (response.getSparseVector() != null) {
                sparseJson = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(response.getSparseVector());
            }

            vectorStoreMapper.insertFaqVector(faqId, question, answer,
                    null, null, combinedEmbeddingBytes, sparseJson);
            return 1;
        } catch (Exception e) {
            System.err.println("FAQ向量化失败：" + question + " - " + e.getMessage());
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

        try {
            Map<String, Object> docParams = new HashMap<>();
            docParams.put("docName", sourceFileName);
            docParams.put("docPath", "upload/" + sourceFileName);
            docParams.put("docContent", rawText);
            docParams.put("docType", getFileExtension(sourceFileName));

            int totalChunks = (rawText.length() + 500 - 1) / 500;
            docParams.put("chunkCount", totalChunks);

            knowledgeDocMapper.insertKnowledgeDoc(docParams);
            Object docIdObj = docParams.get("docId");
            if (docIdObj == null) {
                System.err.println("文档插入数据库失败，未获取到 docId: " + sourceFileName);
                return 0;
            }
            int docId = ((Number) docIdObj).intValue();

            int chunkSize = 500;
            int chunkIndex = 0;

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            for (int i = 0; i < rawText.length(); i += chunkSize) {
                int end = Math.min(rawText.length(), i + chunkSize);
                String chunkContent = rawText.substring(i, end);

                try {
                    VectorEmbeddingResponse response = vectorService.embedText(chunkContent);
                    if (response.isSuccess() && response.getDenseVector() != null) {
                        byte[] chunkEmbeddingBytes = floatsToBytes(response.getDenseVector());
                        String metadata = "{\"source\":\"" + sourceFileName + "\",\"chunk\":" + chunkIndex + "}";

                        String sparseJson = null;
                        if (response.getSparseVector() != null) {
                            sparseJson = objectMapper.writeValueAsString(response.getSparseVector());
                        }

                        vectorStoreMapper.insertKnowledgeVector(docId, chunkIndex, chunkContent, chunkEmbeddingBytes, metadata, sparseJson);
                        insertedChunks++;
                    }
                } catch (Exception e) {
                    System.err.println("文档分块向量化失败，分块索引：" + chunkIndex + " - " + e.getMessage());
                }
                chunkIndex++;
            }
        } catch (Exception e) {
            System.err.println("文档处理异常：" + sourceFileName + " - " + e.getMessage());
        }
        return insertedChunks;
    }

    private String getFileExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx < fileName.length() - 1) {
            return fileName.substring(dotIdx + 1).toLowerCase();
        }
        return "unknown";
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