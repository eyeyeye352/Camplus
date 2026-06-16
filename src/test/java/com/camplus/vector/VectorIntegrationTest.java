package com.camplus.vector;

import com.camplus.vector.pojo.VectorSearchResult;
import com.camplus.vector.service.AnswerGenerationService;
import com.camplus.vector.service.BgeM3OnnxService;
import com.camplus.vector.service.VectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量服务集成测试
 * 测试完整流程：向量化 -> 检索 -> 生成答案
 */
@SpringBootTest
public class VectorIntegrationTest {

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @Autowired
    private VectorService vectorService;

    @Autowired
    private AnswerGenerationService answerGenerationService;

    @Test
    public void testCompleteRagFlow() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过集成测试");
            return;
        }

        String question = "校园生活";

        // 步骤1：向量化
        System.out.println("=== 步骤1：向量化 ===");
        var embeddingResponse = vectorService.embedText(question);
        assertNotNull(embeddingResponse);
        assertTrue(embeddingResponse.isSuccess(), "向量化应成功");
        assertNotNull(embeddingResponse.getDenseVector());
        System.out.println("稠密向量维度: " + embeddingResponse.getDenseVector().length);

        // 步骤2：检索FAQ
        System.out.println("=== 步骤2：检索FAQ ===");
        List<VectorSearchResult> faqResults = vectorService.search("faq_vector_store", question);
        assertNotNull(faqResults);
        assertTrue(faqResults.size() <= 1, "应最多返回1条结果");
        System.out.println("FAQ检索结果数: " + faqResults.size());

        // 步骤3：检索知识库
        System.out.println("=== 步骤3：检索知识库 ===");
        List<VectorSearchResult> knowledgeResults = vectorService.search("knowledge_vector_store", question);
        assertNotNull(knowledgeResults);
        assertTrue(knowledgeResults.size() <= 1, "应最多返回1条结果");
        System.out.println("知识库检索结果数: " + knowledgeResults.size());
    }

    @Test
    public void testBothVectorTables() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        // 验证支持的两张表都能正常调用
        String query = "测试查询";

        List<VectorSearchResult> faqResults = vectorService.search("faq_vector_store", query);
        assertNotNull(faqResults);

        List<VectorSearchResult> knowledgeResults = vectorService.search("knowledge_vector_store", query);
        assertNotNull(knowledgeResults);

        System.out.println("FAQ表结果数: " + faqResults.size());
        System.out.println("知识库表结果数: " + knowledgeResults.size());
    }

    @Test
    public void testSimilarityScoreConsistency() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        // 同一查询多次调用，结果应一致
        String query = "选课";
        List<VectorSearchResult> results1 = vectorService.search("faq_vector_store", query);
        List<VectorSearchResult> results2 = vectorService.search("faq_vector_store", query);

        assertEquals(results1.size(), results2.size());

        if (!results1.isEmpty() && !results2.isEmpty()) {
            // 相似度分数应一致
            assertEquals(results1.get(0).getSimilarityScore(), 
                        results2.get(0).getSimilarityScore(), 0.001f);
        }
    }
}
