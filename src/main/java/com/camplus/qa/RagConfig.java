package com.camplus.qa;

import com.camplus.vector.pojo.VectorSearchResult;
import com.camplus.vector.service.VectorService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    private static final float FAQ_MIN_SCORE = 0.55f;
    private static final float FAQ_DENSE_WEIGHT = 0.9f;
    private static final float FAQ_SPARSE_WEIGHT = 0.1f;
    private static final float DOC_DENSE_WEIGHT = 0.5f;
    private static final float DOC_SPARSE_WEIGHT = 0.5f;

    @Autowired
    private VectorService vectorService;

    @Value("${ollama.chat-model-name:qwen2.5:7b}")
    private String chatModelName;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Bean
    public CampusAssistant campusAssistant() {

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModelName)
                .temperature(0.7)
                .build();

        ContentRetriever contentRetriever = query -> {
            String question = query.text();

            // 第一步：优先检索 FAQ（阈值 0.6，dense 0.9 + sparse 0.1）
            List<VectorSearchResult> faqResults =
                    vectorService.search("faq_vector_store", question, FAQ_MIN_SCORE, 1,
                            FAQ_DENSE_WEIGHT, FAQ_SPARSE_WEIGHT);

            int faqCount = faqResults != null ? faqResults.size() : 0;
            List<Content> contents = new ArrayList<>();

            if (faqCount > 0) {
                log.info("[RAG检索] FAQ命中={}条, 使用FAQ回答 (dense={}, sparse={})",
                        faqCount, FAQ_DENSE_WEIGHT, FAQ_SPARSE_WEIGHT);
                for (VectorSearchResult res : faqResults) {
                    contents.add(Content.from(res.getContent()));
                }
            } else {
                // FAQ 未命中，回退到文档检索（无阈值，Top 1，dense 0.5 + sparse 0.5）
                log.info("[RAG检索] FAQ未命中, 回退到文档检索 (无阈值,Top1,dense={},sparse={})",
                        DOC_DENSE_WEIGHT, DOC_SPARSE_WEIGHT);
                List<VectorSearchResult> docResults =
                        vectorService.search("knowledge_vector_store", question, 0f, 1,
                                DOC_DENSE_WEIGHT, DOC_SPARSE_WEIGHT);

                int docCount = docResults != null ? docResults.size() : 0;
                if (docCount > 0) {
                    for (VectorSearchResult res : docResults) {
                        contents.add(Content.from(res.getContent()));
                    }
                }
                log.info("[RAG检索] 文档匹配={}条", docCount);
            }

            return contents;
        };

        return AiServices.builder(CampusAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }
}
