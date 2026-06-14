package com.example.campusqa.qa;

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

        // 读取 docs 文件夹下的校园文件（请确保路径与你实际项目结构一致）
        Path documentPath = Paths.get("src/main/resources/docs");
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(documentPath);

        // 利用本地 ONNX 模型进行向量化切片计算
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);

        System.out.println("====== 🚀 本地向量库计算完毕！Ollama 本地大模型已就绪！ ======");

        // 配置检索器（从本地库寻找答案片段）
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();

        // 组装最终的 AI 助手
        return AiServices.builder(CampusAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // 开启多轮对话记忆
                .contentRetriever(contentRetriever)
                .build();
    }
}