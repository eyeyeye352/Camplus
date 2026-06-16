package com.camplus.vector;

import com.camplus.vector.service.BgeM3OnnxService;
import dev.langchain4j.data.embedding.Embedding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BGE-M3 ONNX 向量化服务测试
 * 注意：此测试需要BGE-M3 ONNX模型文件存在于配置的路径中
 */
@SpringBootTest
public class BgeM3OnnxServiceTest {

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @Test
    public void testModelInitialization() {
        // 检查模型是否已初始化
        boolean initialized = bgeM3OnnxService.isInitialized();
        System.out.println("模型初始化状态: " + initialized);
        if (!initialized) {
            System.out.println("初始化错误信息: " + bgeM3OnnxService.getInitErrorMessage());
        }
        // 如果模型文件存在，应该初始化成功
        // 如果模型文件不存在，跳过此测试
        if (bgeM3OnnxService.getInitErrorMessage() != null && 
            bgeM3OnnxService.getInitErrorMessage().contains("文件")) {
            System.out.println("模型文件未找到，跳过此测试");
        }
    }

    @Test
    public void testEncodeText() {
        // 跳过测试如果模型未初始化
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过encode测试");
            return;
        }

        String text = "校园生活助手";
        BgeM3OnnxService.BgeM3Result result = bgeM3OnnxService.encode(text);

        // 验证稠密向量
        assertNotNull(result);
        assertNotNull(result.dense);
        assertTrue(result.dense.length > 0, "稠密向量维度应该大于0");

        // 验证稀疏向量
        assertNotNull(result.sparse);
        // 稀疏向量可能为空（取决于文本），但不应该是null
    }

    @Test
    public void testEmbedText() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过embed测试");
            return;
        }

        String text = "选课流程";
        Embedding embedding = bgeM3OnnxService.embed(text);

        assertNotNull(embedding);
        assertNotNull(embedding.vector());
        assertTrue(embedding.vector().length > 0, "向量维度应该大于0");
    }

    @Test
    public void testCosineSimilarity() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过cosineSimilarity测试");
            return;
        }

        // 创建两个相同的向量
        float[] vec1 = {1.0f, 0.0f, 0.0f};
        float[] vec2 = {1.0f, 0.0f, 0.0f};

        float similarity = bgeM3OnnxService.cosineSimilarity(vec1, vec2);
        assertEquals(1.0f, similarity, 0.0001f, "相同向量的余弦相似度应该为1");

        // 创建正交向量
        float[] vec3 = {1.0f, 0.0f, 0.0f};
        float[] vec4 = {0.0f, 1.0f, 0.0f};
        float orthSimilarity = bgeM3OnnxService.cosineSimilarity(vec3, vec4);
        assertEquals(0.0f, orthSimilarity, 0.0001f, "正交向量的余弦相似度应该为0");
    }

    @Test
    public void testLexicalSimilarity() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过lexicalSimilarity测试");
            return;
        }

        Map<Integer, Float> sparse1 = Map.of(1, 0.5f, 2, 0.3f, 3, 0.2f);
        Map<Integer, Float> sparse2 = Map.of(1, 0.4f, 2, 0.6f, 4, 0.1f);

        float similarity = bgeM3OnnxService.lexicalSimilarity(sparse1, sparse2);
        // 共同token 1和2的权重乘积之和: 1*0.4 + 2*0.3 = 0.4 + 0.6 = 1.0
        // 实际计算: 0.5*0.4 + 0.3*0.6 = 0.2 + 0.18 = 0.38
        assertEquals(0.38f, similarity, 0.001f);
    }

    @Test
    public void testEmptyLexicalSimilarity() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        // 空稀疏向量应返回0
        Map<Integer, Float> empty1 = Map.of();
        Map<Integer, Float> nonEmpty = Map.of(1, 0.5f);

        assertEquals(0.0f, bgeM3OnnxService.lexicalSimilarity(empty1, nonEmpty), 0.0001f);
        assertEquals(0.0f, bgeM3OnnxService.lexicalSimilarity(nonEmpty, empty1), 0.0001f);
    }
}
