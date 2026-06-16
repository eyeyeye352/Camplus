package com.camplus.vector.controller;

import com.camplus.vector.pojo.AnswerGenerationResponse;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.pojo.VectorSearchResult;
import com.camplus.vector.service.AnswerGenerationService;
import com.camplus.vector.service.BgeM3OnnxService;
import com.camplus.vector.service.VectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vector")
@CrossOrigin(origins = "*")
public class VectorController {

    @Autowired
    private VectorService vectorService;

    @Autowired
    private AnswerGenerationService answerGenerationService;

    @Autowired
    private BgeM3OnnxService bgeM3OnnxService;

    @PostMapping("/embed")
    public VectorEmbeddingResponse embedText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return vectorService.embedText(text);
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String, Object> request) {
        String tableName = (String) request.get("tableName");
        String queryText = (String) request.get("queryText");

        List<VectorSearchResult> results = vectorService.search(tableName, queryText);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("results", results);
        response.put("totalCount", results.size());
        return response;
    }

    @PostMapping("/generate")
    public AnswerGenerationResponse generateAnswer(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        List<String> contexts = (List<String>) request.get("contexts");
        return answerGenerationService.generateAnswer(question, contexts);
    }

    @PostMapping("/rag")
    public AnswerGenerationResponse ragAnswer(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        return answerGenerationService.ragAnswer(question);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("modelInitialized", bgeM3OnnxService.isInitialized());
        if (!bgeM3OnnxService.isInitialized()) {
            response.put("error", bgeM3OnnxService.getInitErrorMessage());
        }
        return response;
    }
}
