package com.camplus.admin.service.impl;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import com.camplus.admin.service.KnowledgeExtractService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KnowledgeExtractServiceImpl implements KnowledgeExtractService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeExtractServiceImpl.class);

    private final String RAW_DATA_DIR = System.getProperty("user.dir") + File.separator + "RawData";

    @Override
    public List<KnowledgeExtractDTO> saveAndExtractUploadedFile(MultipartFile file) throws Exception {
        File targetDir = new File(RAW_DATA_DIR);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File localFile = new File(targetDir, file.getOriginalFilename());
        file.transferTo(localFile);

        return extractFromFile(localFile);
    }

    @Override
    public List<KnowledgeExtractDTO> initDataFromRawDataDirectory() throws Exception {
        List<KnowledgeExtractDTO> allExtractedData = new ArrayList<>();
        File rawDataDir = new File(RAW_DATA_DIR);

        if (!rawDataDir.exists() || !rawDataDir.isDirectory()) {
            return allExtractedData;
        }

        File[] files = rawDataDir.listFiles();
        if (files == null) return allExtractedData;

        for (File file : files) {
            String fileName = file.getName();
            // 过滤无效文件
            if (fileName.equals(".gitkeep") || fileName.endsWith(".bat") || fileName.equals("application.properties")) {
                continue;
            }
            allExtractedData.addAll(extractFromFile(file));
        }
        return allExtractedData;
    }

    /**
     * 核心路由：根据文件后缀名调用不同的文本提取策略
     * FAQ.txt 文件特殊处理：解析 Q:/A: 格式的问答对
     */
    private List<KnowledgeExtractDTO> extractFromFile(File file) {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        String fileName = file.getName();

        try {
            if (fileName.equals("FAQ.txt")) {
                list.addAll(parseFaqFile(file));
            } else if (fileName.toLowerCase().endsWith(".csv")) {
                list.addAll(parseCsv(file));
            } else if (fileName.toLowerCase().endsWith(".txt")) {
                list.addAll(parseTxt(file));
            } else if (fileName.toLowerCase().endsWith(".pdf")) {
                list.addAll(parsePdf(file));
            } else if (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc")) {
                list.addAll(parseWord(file));
            } else {
                log.warn("暂不支持解析此类文件: {}", fileName);
            }
        } catch (Exception e) {
            log.error("解析文件失败: {} - {}", file.getName(), e.getMessage(), e);
        }

        return list;
    }

    /**
     * 解析 FAQ.txt 文件，格式为 Q: 问题 / A: 答案 的问答对
     */
    private List<KnowledgeExtractDTO> parseFaqFile(File file) throws Exception {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        Pattern pattern = Pattern.compile("Q:\\s*(.+?)\\s*A:\\s*(.+?)(?=\\s*Q:|$)",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String question = matcher.group(1).trim();
            String answer = matcher.group(2).trim();
            if (!question.isEmpty() && !answer.isEmpty()) {
                list.add(new KnowledgeExtractDTO(file.getName(), "TYPE_FAQ", question, answer));
            }
        }

        log.info("FAQ解析完成: {} ({} 条问答对)", file.getName(), list.size());
        return list;
    }

    // ==========================================
    // 以下为具体的文本解析策略 (解析器)
    // ==========================================

    /**
     * 1. 解析 PDF 文件
     */
    private List<KnowledgeExtractDTO> parsePdf(File file) throws Exception {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        // 使用 PDFBox 读取 PDF
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 清理掉多余的换行符和首尾空格
            text = text.replaceAll("[\\r\\n]+", "\n").trim();

            if (!text.isEmpty()) {
                list.add(new KnowledgeExtractDTO(file.getName(), "TYPE_DOC", file.getName(), text));
            }
        }
        return list;
    }

    /**
     * 2. 解析 Word 文件 (兼容 .docx 和 .doc)
     */
    private List<KnowledgeExtractDTO> parseWord(File file) throws Exception {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        String text = "";

        try (FileInputStream fis = new FileInputStream(file)) {
            if (file.getName().toLowerCase().endsWith(".docx")) {
                // 处理新版 Word
                try (XWPFDocument document = new XWPFDocument(fis);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    text = extractor.getText();
                }
            } else {
                // 处理旧版 Word
                try (HWPFDocument document = new HWPFDocument(fis);
                     WordExtractor extractor = new WordExtractor(document)) {
                    text = extractor.getText();
                }
            }
        }

        // 简单清洗文本
        text = text.replaceAll("[\\r\\n]+", "\n").trim();
        if (!text.isEmpty()) {
            list.add(new KnowledgeExtractDTO(file.getName(), "TYPE_DOC", file.getName(), text));
        }
        return list;
    }

    /**
     * 3. 解析 TXT 文件
     */
    private List<KnowledgeExtractDTO> parseTxt(File file) throws Exception {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        text = text.replaceAll("[\\r\\n]+", "\n").trim();
        if (!text.isEmpty()) {
            list.add(new KnowledgeExtractDTO(file.getName(), "TYPE_DOC", file.getName(), text));
        }
        return list;
    }

    /**
     * 4. 解析 CSV 文件 (改良版：完美支持问答文本内部包含英文逗号及转义双引号)
     */
    private List<KnowledgeExtractDTO> parseCsv(File file) throws Exception {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                // 【核心修复】调用智能安全切分算法，效仿 split(regex, 2) 且识别引号隔离区
                String[] columns = parseCsvLineLimit2(line);
                if (columns.length >= 2) {
                    String question = columns[0];
                    String answer = columns[1];
                    list.add(new KnowledgeExtractDTO(file.getName(), "TYPE_FAQ", question, answer));
                }
            }
        }
        return list;
    }

    /**
     * 核心辅助方法：在忽略双引号内部逗号的前提下，只切分第一个合法的英文逗号（实现安全的 split(..., 2) 效果）
     */
    private String[] parseCsvLineLimit2(String line) {
        boolean inQuotes = false;
        int splitIdx = -1;

        // 线性扫描整行字符，动态跟踪是否处于双引号的“内容隔离区”
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes; // 状态取反：进入或离开双引号区域
            } else if (c == ',' && !inQuotes) {
                splitIdx = i; // 找到了第一个真正用于隔离【问题】与【答案】的合法逗号
                break;
            }
        }

        // 如果整行没找到引号外部的合法逗号，说明无法切分成 Q&A 结构
        if (splitIdx == -1) {
            return new String[]{line};
        }

        // 精准切分为前后两部分（第一列为 Question，后面剩余的全部归为 Answer）
        String part1 = line.substring(0, splitIdx).trim();
        String part2 = line.substring(splitIdx + 1).trim();

        // 深度清洗两部分数据（去除标准 CSV 自动加上的外层双引号，并还原内部转义）
        return new String[]{cleanCsvField(part1), cleanCsvField(part2)};
    }

    /**
     * 彻底清洗 CSV 字段文本：去掉外层包裹的双引号，并将内部转义双写 "" 还原为单引号 "
     */
    private String cleanCsvField(String field) {
        // 1. 检查并剥离 CSV 自动为“包含逗号的文本”包裹的外层双引号
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            field = field.substring(1, field.length() - 1);
        }
        // 2. 还原标准 CSV 中被双写转义的引号（例如：把文本内部的 "" 还原为真正的 "）
        return field.replace("\"\"", "\"").trim();
    }
}