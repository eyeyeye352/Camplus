package com.camplus.admin.controller;

import com.camplus.admin.pojo.KnowledgeExtractDTO;
import com.camplus.admin.service.KnowledgeExtractService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/knowledge") // 路由扩展，不仅限faq
public class KnowledgeImportController {

    private final KnowledgeExtractService extractService;

    // 【修改说明1】删除了原有的 FaqItemMapper 依赖注入，彻底与数据库解耦
    // 【修改说明2】注入了全新的 KnowledgeExtractService 来处理文件流转
    public KnowledgeImportController(KnowledgeExtractService extractService) {
        this.extractService = extractService;
    }

    /**
     * 接口1：处理管理员前端上传的单个/多个文件
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadAndExtract(@RequestParam("file") MultipartFile file) {
        Map<String, Object> jsonResponse = new HashMap<>();

        if (file.isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "上传的文件是空的");
            return jsonResponse;
        }

        try {
            // 将文件落盘并提取文本
            List<KnowledgeExtractDTO> extractedData = extractService.saveAndExtractUploadedFile(file);

            // 【后续交接点】这里提取出的 extractedData 就是大模型需要的纯文本
            // 目前作为演示，直接返回给前端。实际生产中，你可以将其推入 MQ 或异步转交给模型组员

            jsonResponse.put("success", true);
            jsonResponse.put("msg", "文件上传并解析成功，已存入 RawData 目录");
            jsonResponse.put("dataCount", extractedData.size());
            jsonResponse.put("extractedData", extractedData);

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "文件处理异常: " + e.getMessage());
        }

        return jsonResponse;
    }

    /**
     * 接口2：全新功能，一键初始化本地 RawData 目录下的所有文件
     */
    @PostMapping("/initLocal")
    public Map<String, Object> initFromLocalDir() {
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            // 扫描目录并批量提取文本
            List<KnowledgeExtractDTO> allExtractedData = extractService.initDataFromRawDataDirectory();

            jsonResponse.put("success", true);
            jsonResponse.put("msg", "本地目录扫描初始化成功");
            jsonResponse.put("fileOrRecordCount", allExtractedData.size());
            // jsonResponse.put("extractedData", allExtractedData); // 如果数据量极大，建议注释掉此行防止前端卡死

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "本地目录初始化异常: " + e.getMessage());
        }

        return jsonResponse;
    }
}