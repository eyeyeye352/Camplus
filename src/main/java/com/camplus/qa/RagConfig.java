package com.camplus.qa;

import com.camplus.vector.pojo.VectorSearchResult;
import com.camplus.vector.service.VectorService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RagConfig {

    // 【大融合1】注入同学B写的向量检索服务
    @Autowired
    private VectorService vectorService;

    @Value("${ollama.chat-model-name:qwen2.5:7b}")
    private String chatModelName;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Bean
    public CampusAssistant campusAssistant() {

        // 1. Ollama 在这里只安安心心负责“聊天和答案生成”，完全不负责“向量化”
        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModelName)
                .temperature(0.7)
                .build();

        // 2.【大融合2：核心关键】自定义检索器
        // 废弃原先的 InMemory 内存库，直接让 LangChain4j 在需要上下文时，去调同学B写的 MySQL 检索逻辑！
        ContentRetriever contentRetriever = query -> {
            String question = query.text(); // 获取用户前台提问的文本

            // 联动同学B在 VectorServiceImpl里实现的 search 方法
            // 该方法内部会自动触发本地ONNX向量化，并去 MySQL 的 knowledge_vector_store 表里查找匹配片段
            List<VectorSearchResult> searchResults =
                    vectorService.search("knowledge_vector_store", question);

            List<Content> contents = new ArrayList<>();
            if (searchResults != null) {
                for (VectorSearchResult res : searchResults) {
                    contents.add(Content.from(res.getContent()));
                }
            }
            return contents;
        }; // 🌟 这里已经帮你加上了结束括号

        // 3. 构建大模型智能助手，挂载我们融合后的内容检索器
        return AiServices.builder(CampusAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }
}