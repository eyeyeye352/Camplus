package com.example.campusqa.qa;

import org.springframework.web.bind.annotation.*;
@CrossOrigin
@RestController
@RequestMapping("/api/qa")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    // 利用 Java 17 的 Record 快速定义数据载体
    public record QaRequest(String question) {}
    public record QaResponse(String answer) {}

    @PostMapping("/ask")
    public QaResponse askQuestion(@RequestBody QaRequest request) {
        String answer = qaService.generateAnswer(request.question());
        return new QaResponse(answer);
    }
}
