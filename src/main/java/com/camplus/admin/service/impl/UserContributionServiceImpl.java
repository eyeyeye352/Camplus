package com.camplus.admin.service.impl;

import com.camplus.admin.Mappers.UserContributionMapper;
import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
import com.camplus.admin.service.UserContributionService;
import com.camplus.faq.mappers.FaqMapper;
import com.camplus.vector.mappers.VectorStoreMapper;
import com.camplus.vector.pojo.VectorEmbeddingResponse;
import com.camplus.vector.service.VectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserContributionServiceImpl implements UserContributionService {
    private static final Logger log = LoggerFactory.getLogger(UserContributionServiceImpl.class);

    private final UserContributionMapper contributionMapper;
    private final FaqMapper faqMapper;
    private final VectorStoreMapper vectorStoreMapper;
    private final VectorService vectorService;

    public UserContributionServiceImpl(UserContributionMapper contributionMapper, 
                                       FaqMapper faqMapper,
                                       VectorStoreMapper vectorStoreMapper,
                                       VectorService vectorService) {
        this.contributionMapper = contributionMapper;
        this.faqMapper = faqMapper;
        this.vectorStoreMapper = vectorStoreMapper;
        this.vectorService = vectorService;
    }

    @Override
    public List<UserContribution> getPendingList() {
        return contributionMapper.selectPendingContributions();
    }

    @Override
    public List<UserContribution> getAllContributions() {
        return contributionMapper.selectAllContributions();
    }

    @Override
    @Transactional
    public boolean reviewContribution(ReviewRequestDTO dto) {
        try {
            UserContribution originalRecord = contributionMapper.selectById(dto.getContributionId());
            if (originalRecord == null || originalRecord.getStatus() != 0) {
                return false;
            }

            int rows = contributionMapper.updateReviewData(
                    dto.getContributionId(), dto.getStatus(), dto.getComment()
            );

            if (rows > 0 && dto.getStatus() == 1) {
                String finalQuestion = (dto.getFinalQuestion() != null && !dto.getFinalQuestion().isEmpty())
                        ? dto.getFinalQuestion() : originalRecord.getTitle();
                String finalAnswer = (dto.getFinalAnswer() != null && !dto.getFinalAnswer().isEmpty())
                        ? dto.getFinalAnswer() : originalRecord.getContent();

                saveToFaq(finalQuestion, finalAnswer);
            }

            return rows > 0;

        } catch (Exception e) {
            log.error("审核贡献失败: {}", e.getMessage());
            return false;
        }
    }

    private void saveToFaq(String question, String answer) {
        try {
            Map<String, Object> faqParams = new HashMap<>();
            faqParams.put("question", question);
            faqParams.put("answer", answer);
            faqParams.put("source", "contribution");

            faqMapper.insertFaq(faqParams);
            Object faqIdObj = faqParams.get("faqId");
            if (faqIdObj == null) {
                log.warn("审核通过保存FAQ失败，未获取到 faqId");
                return;
            }
            int faqId = ((Number) faqIdObj).intValue();

            String combinedText = "问题：" + question + "\n答案：" + answer;
            VectorEmbeddingResponse response = vectorService.embedText(combinedText);
            if (!response.isSuccess() || response.getDenseVector() == null) {
                log.warn("审核通过保存FAQ向量化失败");
                return;
            }

            byte[] combinedEmbeddingBytes = floatsToBytes(response.getDenseVector());
            String sparseJson = sparseToJson(response.getSparseVector());

            vectorStoreMapper.insertFaqVector(faqId, question, answer,
                    null, null, combinedEmbeddingBytes, sparseJson);

            log.info("审核通过，已保存FAQ [faqId={}]: {}", faqId,
                    question.length() > 30 ? question.substring(0, 30) + "..." : question);
        } catch (Exception e) {
            log.error("审核通过保存FAQ失败: {}", e.getMessage());
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