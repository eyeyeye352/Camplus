package com.camplus.admin.service.impl;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import com.camplus.admin.service.KnowledgeExtractService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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

@Service
public class KnowledgeExtractServiceImpl implements KnowledgeExtractService {

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
     */
    private List<KnowledgeExtractDTO> extractFromFile(File file) {
        List<KnowledgeExtractDTO> list = new ArrayList<>();
        String fileName = file.getName().toLowerCase();

        try {
            if (fileName.endsWith(".csv")) {
                list.addAll(parseCsv(file));
            } else if (fileName.endsWith(".txt")) {
                list.addAll(parseTxt(file));
            } else if (fileName.endsWith(".pdf")) {
                list.addAll(parsePdf(file));
            } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
                list.addAll(parseWord(file));
            } else {
                System.out.println("【警告】暂不支持解析此类文件: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("【错误】解析文件失败: " + file.getName() + "，原因：" + e.getMessage());
            e.printStackTrace();
        }

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
     * 4. 解析 CSV 文件 (保留原有的 Q&A 形式，以防有需要)
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

                String[] columns = line.split(",", 2);
                if (columns.length >= 2) {
                    String question = columns[0].trim().replace("\"", "");
                    String answer = columns[1].trim().replace("\"", "");
                    list.add(new KnowledgeExtractDTO(file.getName(), "TYPE_FAQ", question, answer));
                }
            }
        }
        return list;
    }
}