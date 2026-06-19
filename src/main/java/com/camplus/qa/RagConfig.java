package com.camplus.qa;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfig {

    // 1. 接入本地 Ollama 大模型
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // Ollama 默认本地端口
                .modelName("qwen2.5:7b ")           // 严格匹配你下载的模型名称
                .temperature(0.7)
                .build();
    }

    // 2. 接入本地硬编码的 BGE-M3-ONNX 向量模型
    // 2. 接入本地 Ollama 运行的 BGE-M3 向量模型
    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434") // Ollama 默认本地端口
                .modelName("bge-m3")               // 调用你刚才 pull 下来的模型
                .build();
    }

    // 3. 内存向量数据库
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    // 4. 将知识库装载并构建大模型助手
    @Bean
    public CampusAssistant campusAssistant(ChatLanguageModel chatModel,
                                           EmbeddingModel embeddingModel,
                                           EmbeddingStore<TextSegment> embeddingStore) {

        // 【优化点】读取文档和灌库的逻辑，包裹在 CompletableFuture 异步线程中执行
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("⏳ [后台线程] 开始异步解析和导入本地知识库...");
                long startTime = System.currentTimeMillis();

                // 读取 docs 文件夹
                Path documentPath = Paths.get("src/main/resources/docs");
                List<Document> documents = FileSystemDocumentLoader.loadDocuments(documentPath);

                // 构建切片器
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .documentSplitter(DocumentSplitters.recursive(300, 30))
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();

                // 异步执行耗时的灌库操作
                ingestor.ingest(documents);

                long endTime = System.currentTimeMillis();
                System.out.println("🚀 [后台线程] 本地向量库计算完毕！耗时: " + (endTime - startTime) / 1000 + " 秒");

            } catch (Exception e) {
                System.err.println("❌ [后台线程] 知识库异步导入失败: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // 核心改动：不再等待上面执行完，直接返回助手实例，让 Spring Boot 瞬间启动成功！
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();

        return AiServices.builder(CampusAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }
}
