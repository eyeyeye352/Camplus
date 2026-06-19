package com.camplus.qa;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qa")
@CrossOrigin(origins = "*") // 暴力解除跨域限制，方便局域网内前端同学联调
public class QaController {

    private final CampusAssistant campusAssistant;

    public QaController(CampusAssistant campusAssistant) {
        this.campusAssistant = campusAssistant;
    }

    @PostMapping("/ask")
    public Map<String, String> askQuestion(@RequestBody Map<String, String> request) {
        // 提取前端发来的问题
        String question = request.get("question");

        // 唤醒本地的 Qwen2.5 大模型开始思考
        String answer = campusAssistant.answer(question);

        // 封装成 JSON 格式返回给前端
        Map<String, String> response = new HashMap<>();
        response.put("answer", answer);

        return response;
    }
}