package com.camplus.admin.runner;

import com.camplus.admin.service.DataImportService;
import com.camplus.faq.mappers.FaqMapper;
import com.camplus.vector.mappers.KnowledgeDocMapper;
import com.camplus.vector.service.BgeM3OnnxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * 数据导入启动器
 *
 * 两种触发模式：
 * 1. --import-only 模式（建库脚本调用）：以非Web模式启动，执行导入后直接退出
 * 2. 正常启动模式：检测空表时自动导入，不退出
 *
 * 导入条件：faq_items 和 knowledge_docs 表均为空，且 RawData 目录存在有效文件
 */
@Component
@Order(100)
public class DataImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataImportRunner.class);

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private FaqMapper faqMapper;

    @Autowired
    private KnowledgeDocMapper knowledgeDocMapper;

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @Override
    public void run(String... args) {
        boolean importOnly = Arrays.asList(args).contains("--import-only");

        if (importOnly) {
            log.info("运行在 --import-only 模式，数据库凭据已通过系统属性注入");
        }

        String rawDataPath = System.getProperty("user.dir") + File.separator + "RawData";
        File rawDataDir = new File(rawDataPath);

        if (!rawDataDir.exists() || !rawDataDir.isDirectory()) {
            log.info("RawData 目录不存在，跳过数据导入");
            if (importOnly) System.exit(0);
            return;
        }

        File[] files = rawDataDir.listFiles();
        if (files == null || files.length == 0) {
            log.info("RawData 目录为空，跳过数据导入");
            if (importOnly) System.exit(0);
            return;
        }

        boolean hasValidFile = false;
        for (File f : files) {
            String name = f.getName();
            if (!name.equals(".gitkeep") && !name.endsWith(".bat")) {
                hasValidFile = true;
                break;
            }
        }
        if (!hasValidFile) {
            log.info("RawData 目录中没有有效文件，跳过数据导入");
            if (importOnly) System.exit(0);
            return;
        }

        int faqCount = 0;
        int docCount = 0;
        try {
            faqCount = faqMapper.countAll();
            docCount = knowledgeDocMapper.countAll();
        } catch (Exception e) {
            log.warn("查询数据库表行数失败，可能表尚未创建: {}", e.getMessage());
            if (importOnly) System.exit(1);
            return;
        }

        if (faqCount > 0 || docCount > 0) {
            log.info("数据库已有数据 (faq_items={}, knowledge_docs={})，跳过数据导入", faqCount, docCount);
            if (importOnly) System.exit(0);
            return;
        }

        log.info("检测到数据库为空且 RawData 目录存在文件，准备导入数据...");

        if (!bgeM3OnnxService.isInitialized()) {
            log.warn("========================================");
            log.warn("BGE-M3 向量化模型未初始化，无法进行数据导入！");
            log.warn("原因: {}", bgeM3OnnxService.getInitErrorMessage());
            log.warn("请确保模型文件路径正确: {}", bgeM3OnnxService.getClass().getSimpleName());
            log.warn("========================================");
            if (importOnly) System.exit(1);
            return;
        }

        log.info("BGE-M3 模型已就绪，开始数据导入...");
        Map<String, Object> result = dataImportService.importFromRawData();

        log.info("========================================");
        log.info("数据导入结果:");
        log.info("  FAQ: {} 条", result.get("faqCount"));
        log.info("  文档: {} 个", result.get("docCount"));
        log.info("  分块: {} 条", result.get("chunkCount"));
        log.info("  失败: {} 条", result.get("failCount"));
        log.info("========================================");

        if (importOnly) {
            log.info("数据导入完成，程序退出。");
            System.exit(0);
        }
    }
}
