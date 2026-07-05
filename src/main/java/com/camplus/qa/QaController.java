package com.camplus.qa;

import com.camplus.faq.mappers.FaqMapper;
import com.camplus.vector.mappers.VectorStoreMapper;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.service.VectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qa")
@CrossOrigin(origins = "*")
public class QaController {

    private static final Logger log = LoggerFactory.getLogger(QaController.class);

    private final CampusAssistant campusAssistant;
    private final FaqMapper faqMapper;
    private final VectorStoreMapper vectorStoreMapper;
    private final VectorService vectorService;

    public QaController(CampusAssistant campusAssistant, FaqMapper faqMapper,
                        VectorStoreMapper vectorStoreMapper, VectorService vectorService) {
        this.campusAssistant = campusAssistant;
        this.faqMapper = faqMapper;
        this.vectorStoreMapper = vectorStoreMapper;
        this.vectorService = vectorService;
    }

    @PostMapping("/ask")
    public Map<String, String> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("[问答] 收到问题: {}", question);

        String answer = campusAssistant.answer(question);

        String preview = answer != null ? (answer.length() > 100 ? answer.substring(0, 100) + "..." : answer) : "null";
        log.info("[问答] 回答: {}", preview);

        boolean faqHit = RagConfig.isFaqHit();
        Integer hitFaqId = RagConfig.getHitFaqId();
        RagConfig.clearFaqHit();
        log.info("[问答] FAQ命中状态: {}, FAQ ID: {}", faqHit ? "命中" : "未命中", hitFaqId);

        if (faqHit && hitFaqId != null) {
            updateFaqStats(hitFaqId);
        }

        if (!faqHit && answer != null && !answer.trim().isEmpty()) {
            if (answer.trim().equals("暂无相关方面的信息")) {
                log.info("[问答] 大模型无法回答，不存入FAQ");
            } else {
                saveToFaq(question, answer);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("answer", answer);
        return response;
    }

    private void updateFaqStats(int faqId) {
        try {
            faqMapper.incrementQuestionCount(faqId);

            Map<String, Object> faq = faqMapper.selectByIdMap(faqId);
            if (faq != null) {
                int questionCount = faq.get("question_count") != null ? ((Number) faq.get("question_count")).intValue() : 0;
                int likeCount = faq.get("like_count") != null ? ((Number) faq.get("like_count")).intValue() : 0;
                int hotScore = questionCount * 10 + likeCount * 5;
                faqMapper.updateHotScore(faqId, hotScore);
                log.info("[问答] 更新FAQ统计成功 [faqId={}]: question_count={}, hot_score={}",
                        faqId, questionCount, hotScore);
            }
        } catch (Exception e) {
            log.error("[问答] 更新FAQ统计失败: {}", e.getMessage());
        }
    }

    private void saveToFaq(String question, String answer) {
        try {
            Map<String, Object> faqParams = new HashMap<>();
            faqParams.put("question", question);
            faqParams.put("answer", answer);
            faqParams.put("source", "auto");

            faqMapper.insertFaq(faqParams);
            Object faqIdObj = faqParams.get("faqId");
            if (faqIdObj == null) {
                log.warn("自动保存FAQ失败，未获取到 faqId");
                return;
            }
            int faqId = ((Number) faqIdObj).intValue();

            String combinedText = "问题：" + question + "\n答案：" + answer;
            VectorEmbeddingResponse response = vectorService.embedText(combinedText);
            if (!response.isSuccess() || response.getDenseVector() == null) {
                log.warn("自动保存FAQ向量化失败");
                return;
            }

            byte[] combinedEmbeddingBytes = floatsToBytes(response.getDenseVector());
            String sparseJson = sparseToJson(response.getSparseVector());

            vectorStoreMapper.insertFaqVector(faqId, question, answer,
                    null, null, combinedEmbeddingBytes, sparseJson);

            log.info("[问答] 自动保存FAQ成功 [faqId={}]: {}", faqId,
                    question.length() > 30 ? question.substring(0, 30) + "..." : question);
        } catch (Exception e) {
            log.error("[问答] 自动保存FAQ失败: {}", e.getMessage());
        }
    }

    private byte[] floatsToBytes(float[] floats) {
        if (floats == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }

    private String sparseToJson(Map<Integer, Float> sparse) {
        if (sparse == null || sparse.isEmpty()) return null;
        try {
            return new ObjectMapper().writeValueAsString(sparse);
        } catch (Exception e) {
            return null;
        }
    }
}