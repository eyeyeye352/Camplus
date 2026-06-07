package com.example.campusqa.qa;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class RagConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    // 🌟 重点改动在这里：直接调用云端的免费中文向量模型，零下载！
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                // ⚠️ 警告：请务必把这里换成你自己在硅基流动申请的真实 sk-... 密钥！
                .apiKey("sk-rffxhncencqyeehwvkylxgddvrhxrbqiqrvyukqlyumhozqv")
                .baseUrl("https://api.siliconflow.cn/v1")
                .modelName("BAAI/bge-m3") // 硅基流动提供的顶级开源免费中文向量模型
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(model)
                .maxResults(2)
                .minScore(0.3) // 调低匹配阈值，保证中文容错率
                .build();
    }

    @Bean
    public CommandLineRunner loadDocument(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
        return args -> {
            System.out.println("====== 正在启动全能文件吞噬者，扫描 docs 文件夹... ======");
            String userDir = System.getProperty("user.dir");
            Path docsDir = Paths.get(userDir, "src", "main", "resources", "docs");

            java.util.List<Document> allDocuments = new java.util.ArrayList<>();

            // 1. 遍历 docs 文件夹下的所有文件
            java.nio.file.Files.walk(docsDir)
                    .filter(java.nio.file.Files::isRegularFile)
                    .forEach(filePath -> {
                        String fileName = filePath.getFileName().toString().toLowerCase();
                        Document doc = null;

                        // 2. 根据文件后缀名，智能选择对应的“解析刀法”
                        if (fileName.endsWith(".txt")) {
                            System.out.println("📄 发现 TXT 文件: " + fileName);
                            doc = FileSystemDocumentLoader.loadDocument(filePath, new TextDocumentParser());
                        } else if (fileName.endsWith(".pdf")) {
                            System.out.println("📕 发现 PDF 文件: " + fileName);
                            doc = FileSystemDocumentLoader.loadDocument(filePath, new dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser());
                        } else if (fileName.endsWith(".docx")) {
                            System.out.println("📘 发现 Word 文件: " + fileName);
                            doc = FileSystemDocumentLoader.loadDocument(filePath, new dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser());
                        }

                        if (doc != null) {
                            allDocuments.add(doc);
                        }
                    });

            if (allDocuments.isEmpty()) {
                System.out.println("⚠️ 警告：docs 文件夹里没有找到任何支持的文档！");
                return;
            }

            // 3. 统一进行向量化切片并存入数据库
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(500, 50)) // 500字一段，保留50字重叠防止语义切断
                    .embeddingModel(model)
                    .embeddingStore(store)
                    .build();

            ingestor.ingest(allDocuments);
            System.out.println("====== 🚀 知识库装载完毕！共吞噬了 " + allDocuments.size() + " 份文档，大模型已通晓一切！ ======");
        };
    }
}