package com.camplus.vector;

import com.camplus.vector.pojo.AnswerGenerationResponse;
import com.camplus.vector.service.AnswerGenerationService;
import com.camplus.vector.service.BgeM3OnnxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 答案生成服务测试
 */
@SpringBootTest
public class AnswerGenerationServiceTest {

    @Autowired
    private AnswerGenerationService answerGenerationService;

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @Test
    public void testGenerateAnswerWithContexts() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        String question = "选课流程是什么？";
        List<String> contexts = Arrays.asList(
            "选课流程包括：1.登录系统 2.选择课程 3.提交选课 4.等待审核",
            "选课时间一般为开学前一周"
        );

        AnswerGenerationResponse response = answerGenerationService.generateAnswer(question, contexts);

        assertNotNull(response);
        // 如果Ollama服务可用
        if (response.isSuccess()) {
            assertNotNull(response.getAnswer());
            assertFalse(response.getAnswer().isEmpty());
            assertNotNull(response.getContextsUsed());
            assertEquals(2, response.getContextsUsed().size());
            assertTrue(response.getConfidence() >= 0 && response.getConfidence() <= 1);
        } else {
            System.out.println("答案生成失败: " + response.getMessage());
        }
    }

    @Test
    public void testGenerateAnswerWithEmptyQuestion() {
        AnswerGenerationResponse response = answerGenerationService.generateAnswer("", Arrays.asList("上下文"));
        assertNotNull(response);
        assertFalse(response.isSuccess(), "空问题应该返回失败");
    }

    @Test
    public void testGenerateAnswerWithNullQuestion() {
        AnswerGenerationResponse response = answerGenerationService.generateAnswer(null, Arrays.asList("上下文"));
        assertNotNull(response);
        assertFalse(response.isSuccess(), "null问题应该返回失败");
    }

    @Test
    public void testGenerateAnswerWithEmptyContexts() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        AnswerGenerationResponse response = answerGenerationService.generateAnswer("测试问题", Arrays.asList());
        assertNotNull(response);
        // 空上下文应仍能调用大模型，只是回答可能不准确
        System.out.println("空上下文答案生成: success=" + response.isSuccess());
    }

    @Test
    public void testRagAnswer() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过rag测试");
            return;
        }

        AnswerGenerationResponse response = answerGenerationService.ragAnswer("学生如何选课？");

        assertNotNull(response);
        if (response.isSuccess()) {
            assertNotNull(response.getAnswer());
            assertFalse(response.getAnswer().isEmpty());
            assertNotNull(response.getContextsUsed());
            assertTrue(response.getConfidence() >= 0 && response.getConfidence() <= 1);
        } else {
            System.out.println("RAG问答失败: " + response.getMessage());
        }
    }

    @Test
    public void testRagAnswerWithEmptyQuestion() {
        AnswerGenerationResponse response = answerGenerationService.ragAnswer("");
        assertNotNull(response);
        assertFalse(response.isSuccess(), "空问题应该返回失败");
    }

    @Test
    public void testRagAnswerWithNullQuestion() {
        AnswerGenerationResponse response = answerGenerationService.ragAnswer(null);
        assertNotNull(response);
        assertFalse(response.isSuccess(), "null问题应该返回失败");
    }

    @Test
    public void testConfidenceRange() {
        if (!bgeM3OnnxService.isInitialized()) {
            System.out.println("模型未初始化，跳过测试");
            return;
        }

        AnswerGenerationResponse response = answerGenerationService.generateAnswer(
            "测试问题",
            Arrays.asList("测试上下文")
        );

        if (response.isSuccess()) {
            assertTrue(response.getConfidence() >= 0.0f, "置信度应>=0");
            assertTrue(response.getConfidence() <= 1.0f, "置信度应<=1");
        }
    }
}
