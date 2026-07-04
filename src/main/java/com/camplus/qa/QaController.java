package com.camplus.qa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qa")
@CrossOrigin(origins = "*")
public class QaController {

    private static final Logger log = LoggerFactory.getLogger(QaController.class);

    private final CampusAssistant campusAssistant;

    public QaController(CampusAssistant campusAssistant) {
        this.campusAssistant = campusAssistant;
    }

    @PostMapping("/ask")
    public Map<String, String> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("[问答] 收到问题: {}", question);

        String answer = campusAssistant.answer(question);

        String preview = answer != null ? (answer.length() > 100 ? answer.substring(0, 100) + "..." : answer) : "null";
        log.info("[问答] 回答: {}", preview);

        Map<String, String> response = new HashMap<>();
        response.put("answer", answer);
        return response;
    }
}
