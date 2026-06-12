package com.camplus.admin.controller;

import com.camplus.admin.Mappers.FaqItemMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/faq")
public class FaqImportController {

    private final FaqItemMapper faqItemMapper;

    // 构造器注入你现有的 FaqItemMapper
    public FaqImportController(FaqItemMapper faqItemMapper) {
        this.faqItemMapper = faqItemMapper;
    }

    @PostMapping("/import")
    public Map<String, Object> importFaqFromCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> jsonResponse = new HashMap<>();

        if (file.isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "上传的文件是空的");
            return jsonResponse;
        }

        int successCount = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            // 逐行读取流，避免大文件撑爆内存
            while ((line = reader.readLine()) != null) {
                // 跳过第一行表头
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                // 拆分标准 CSV 格式的一行数据（问题,答案）
                String[] columns = line.split(",");
                if (columns.length >= 2) {
                    String question = columns[0].trim().replace("\"", "");
                    String answer = columns[1].trim().replace("\"", "");

                    // 直接使用你现有的 Mapper 注入现有的数据库中，display_status 为 1（直接展示）
                    // 对应现有的 faq_items 表的字段结构
                    int result = faqItemMapper.insertFaq(question, answer);
                    if (result > 1 || result == 1) {
                        successCount++;
                    }
                }
            }

            jsonResponse.put("success", true);
            jsonResponse.put("count", successCount);

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("msg", "服务器解析文件异常: " + e.getMessage());
        }

        return jsonResponse;
    }
}
