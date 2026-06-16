package com.camplus.vector;

import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.pojo.VectorSearchResult;
import com.camplus.vector.service.BgeM3OnnxService;
import com.camplus.vector.service.VectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量服务测试
 * 包括文本向量化和向量检索功能
 */
@SpringBootTest
public class VectorServiceTest {

    @Autowired
    private VectorService vectorService;

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @Test
    public void testEmbedText() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过embedText测试");
            return;
        }

        VectorEmbeddingResponse response = vectorService.embedText("校园生活助手");

        assertNotNull(response);
        assertTrue(response.isSuccess(), "向量化应该成功");
        assertNotNull(response.getDenseVector());
        assertNotNull(response.getSparseVector());
        assertEquals("校园生活助手", response.getText());
        // 注意：不再返回dimension字段
    }

    @Test
    public void testEmbedNullText() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        VectorEmbeddingResponse response = vectorService.embedText(null);
        assertNotNull(response);
        assertFalse(response.isSuccess(), "空文本应该返回失败");
    }

    @Test
    public void testEmbedEmptyText() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        VectorEmbeddingResponse response = vectorService.embedText("");
        assertNotNull(response);
        // 空字符串可能成功也可能失败，取决于模型行为
        System.out.println("空文本向量化结果: success=" + response.isSuccess());
    }

    @Test
    public void testEmbedTexts() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过embedTexts测试");
            return;
        }

        List<String> texts = Arrays.asList("选课", "考试", "食堂");
        VectorEmbeddingResponse response = vectorService.embedTexts(texts);

        assertNotNull(response);
        if (response.isSuccess()) {
            assertNotNull(response.getDenseVector());
        }
    }

    @Test
    public void testEmbedEmptyTextsList() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        VectorEmbeddingResponse response = vectorService.embedTexts(Arrays.asList());
        assertNotNull(response);
        assertFalse(response.isSuccess(), "空列表应该返回失败");
    }

    @Test
    public void testSearchFaqVectorStore() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过search测试");
            return;
        }

        List<VectorSearchResult> results = vectorService.search("faq_vector_store", "选课流程");

        assertNotNull(results);
        // 验证固定返回最多1条结果
        assertTrue(results.size() <= 1, "应固定返回最多1条结果，实际返回: " + results.size());
        System.out.println("FAQ检索结果数量: " + results.size());

        if (!results.isEmpty()) {
            VectorSearchResult result = results.get(0);
            assertNotNull(result.getRecordId());
            assertNotNull(result.getContent());
            assertNotNull(result.getTableName());
            assertTrue(result.getSimilarityScore() >= 0.6f, "相似度应大于等于0.6");
        }
    }

    @Test
    public void testSearchKnowledgeVectorStore() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过search测试");
            return;
        }

        List<VectorSearchResult> results = vectorService.search("knowledge_vector_store", "学生手册");

        assertNotNull(results);
        assertTrue(results.size() <= 1, "应固定返回最多1条结果，实际返回: " + results.size());
        System.out.println("知识库检索结果数量: " + results.size());
    }

    @Test
    public void testSearchWithInvalidTableName() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        assertThrows(IllegalArgumentException.class, () -> {
            vectorService.search("invalid_table", "测试查询");
        });
    }

    @Test
    public void testSearchReturnsMaxOneResult() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        // 多次查询验证返回结果数不超过1
        String[] queries = {"选课", "考试", "宿舍", "食堂", "图书馆"};
        for (String query : queries) {
            List<VectorSearchResult> results = vectorService.search("faq_vector_store", query);
            assertTrue(results.size() <= 1, 
                "查询 '" + query + "' 返回结果数应<=1，实际: " + results.size());
        }
    }
}
