package com.camplus.vector.service;

import com.camplus.vector.pojo.AnswerGenerationResponse;
import com.camplus.vector.pojo.VectorSearchResult;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AnswerGenerationService.class);

    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private VectorService vectorService;

    @Value("${ollama.chat-model-name:qwen2.5:7b}")
    private String chatModelName;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @PostConstruct
    public void init() {
        this.chatLanguageModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModelName)
                .temperature(0.7)
                .build();
        log.info("Ollama ChatModel initialized: {} at {}", chatModelName, ollamaBaseUrl);
    }

    public AnswerGenerationResponse generateAnswer(String question, List<String> contexts) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return AnswerGenerationResponse.failure("问题不能为空");
            }

            String prompt = buildPrompt(question, contexts);
            String answer = chatLanguageModel.generate(prompt);

            float confidence = calculateConfidence(answer);

            return AnswerGenerationResponse.success(answer, contexts, confidence);

        } catch (Exception e) {
            log.error("答案生成失败: {}", e.getMessage(), e);
            return AnswerGenerationResponse.failure("答案生成失败: " + e.getMessage());
        }
    }

    public AnswerGenerationResponse ragAnswer(String question) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return AnswerGenerationResponse.failure("问题不能为空");
            }

            List<VectorSearchResult> faqResults = vectorService.search("faq_vector_store", question);
            List<VectorSearchResult> knowledgeResults = vectorService.search("knowledge_vector_store", question);

            List<String> contexts = new ArrayList<>();
            for (VectorSearchResult result : faqResults) {
                contexts.add(result.getContent());
            }
            for (VectorSearchResult result : knowledgeResults) {
                contexts.add(result.getContent());
            }

            if (contexts.isEmpty()) {
                contexts.add("没有找到相关的知识库内容");
            }

            return generateAnswer(question, contexts);

        } catch (Exception e) {
            log.error("RAG问答失败: {}", e.getMessage(), e);
            return AnswerGenerationResponse.failure("RAG问答失败: " + e.getMessage());
        }
    }

    private String buildPrompt(String question, List<String> contexts) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("你是一个校园生活助手，请根据提供的上下文信息回答问题。\n\n");
        promptBuilder.append("上下文信息：\n");

        if (contexts != null && !contexts.isEmpty()) {
            for (int i = 0; i < contexts.size(); i++) {
                promptBuilder.append(i + 1).append(". ").append(contexts.get(i)).append("\n");
            }
        } else {
            promptBuilder.append("无\n");
        }

        promptBuilder.append("\n问题：").append(question).append("\n");
        promptBuilder.append("\n请根据上述上下文信息，给出准确的回答。");
        promptBuilder.append("如果上下文中没有相关信息，请回答\"根据现有信息无法回答该问题\"。");

        return promptBuilder.toString();
    }

    private float calculateConfidence(String answer) {
        if (answer == null || answer.isEmpty()) {
            return 0.0f;
        }

        if (answer.contains("无法回答") || answer.contains("不知道") || answer.contains("不清楚")) {
            return 0.3f;
        }

        if (answer.contains("根据上下文") || answer.contains("根据提供的信息")) {
            return 0.85f;
        }

        return 0.7f;
    }
}
