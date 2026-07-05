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
import java.util.List;
import java.util.Map;

/**
 * 问答控制器
 * 处理用户提问，实现RAG检索流程和FAQ自动学习功能
 */
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

    /**
     * 用户提问接口
     * 流程：1.检查FAQ命中 2.调用大模型回答 3.FAQ命中时更新统计 4.FAQ未命中时自动存入FAQ
     * @param request 包含question字段的请求体
     * @return 包含answer字段的响应
     */
    @PostMapping("/ask")
    public Map<String, String> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("[问答] 收到问题: {}", question);

        Integer hitFaqId = checkFaqHit(question);
        boolean faqHit = hitFaqId != null;

        String answer = campusAssistant.answer(question);

        String preview = answer != null ? (answer.length() > 100 ? answer.substring(0, 100) + "..." : answer) : "null";
        log.info("[问答] 回答: {}", preview);

        log.info("[问答] FAQ命中状态: {}, FAQ ID: {}", faqHit ? "命中" : "未命中", hitFaqId);

        if (faqHit) {
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

    /**
     * 检查问题是否命中FAQ
     * 通过向量检索匹配FAQ库中的问题
     * @param question 用户问题
     * @return 命中的FAQ ID，未命中返回null
     */
    private Integer checkFaqHit(String question) {
        try {
            List<com.camplus.vector.pojo.VectorSearchResult> faqResults = vectorService.search(
                "faq_vector_store", question, 0.55f, 1, 0.9f, 0.1f);
            if (faqResults != null && !faqResults.isEmpty()) {
                return faqResults.get(0).getRecordId().intValue();
            }
        } catch (Exception e) {
            log.error("[问答] 检查FAQ命中失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 更新FAQ统计数据
     * 包括提问次数和热度分（热度分 = 提问次数 × 10）
     * @param faqId FAQ ID
     */
    private void updateFaqStats(int faqId) {
        try {
            faqMapper.incrementQuestionCount(faqId);

            Map<String, Object> faq = faqMapper.selectByIdMap(faqId);
            if (faq != null) {
                int questionCount = faq.get("question_count") != null ? ((Number) faq.get("question_count")).intValue() : 0;
                int hotScore = questionCount * 10;
                faqMapper.updateHotScore(faqId, hotScore);
                log.info("[问答] 更新FAQ统计成功 [faqId={}]: question_count={}, hot_score={}",
                        faqId, questionCount, hotScore);
            }
        } catch (Exception e) {
            log.error("[问答] 更新FAQ统计失败: {}", e.getMessage());
        }
    }

    /**
     * 将问答对自动保存到FAQ表
     * 同时进行向量化处理并存储向量
     * @param question 问题
     * @param answer 答案
     */
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

    /**
     * 将float数组转换为byte数组
     * @param floats float数组
     * @return byte数组
     */
    private byte[] floatsToBytes(float[] floats) {
        if (floats == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }

    /**
     * 将稀疏向量转换为JSON字符串
     * @param sparse 稀疏向量
     * @return JSON字符串
     */
    private String sparseToJson(Map<Integer, Float> sparse) {
        if (sparse == null || sparse.isEmpty()) return null;
        try {
            return new ObjectMapper().writeValueAsString(sparse);
        } catch (Exception e) {
            return null;
        }
    }
}