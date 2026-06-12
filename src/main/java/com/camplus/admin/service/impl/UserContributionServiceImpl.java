package com.camplus.admin.service.impl;

import com.camplus.admin.Mappers.FaqItemMapper;
import com.camplus.admin.Mappers.UserContributionMapper;
import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
import com.camplus.admin.service.UserContributionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserContributionServiceImpl implements UserContributionService {
    private final UserContributionMapper contributionMapper;
    private final FaqItemMapper faqMapper;

    public UserContributionServiceImpl(UserContributionMapper contributionMapper, FaqItemMapper faqMapper) {
        this.contributionMapper = contributionMapper;
        this.faqMapper = faqMapper;
    }

    @Override
    public List<UserContribution> getPendingList() {
        return contributionMapper.selectPendingContributions();
    }

    @Override
    @Transactional
    public boolean reviewContribution(ReviewRequestDTO dto) {
        try {
            // 1. 校验记录是否存在且为待审核
            UserContribution originalRecord = contributionMapper.selectById(dto.getContributionId());
            if (originalRecord == null || originalRecord.getStatus() != 0) {
                return false;
            }

            // 2. 更新贡献表状态与评语
            int rows = contributionMapper.updateReviewData(
                    dto.getContributionId(), dto.getStatus(), dto.getComment()
            );

            // 3. 联动入库：若通过，直接将前端传来的最终 QA 内容写入 faq_items
            if (rows > 0 && dto.getStatus() == 1) {
                // 健壮性容错：如果前端没有传润色后的内容，则兜底使用用户提交的原始 content
                String finalQuestion = (dto.getFinalQuestion() != null && !dto.getFinalQuestion().isEmpty())
                        ? dto.getFinalQuestion() : originalRecord.getTitle();
                String finalAnswer = (dto.getFinalAnswer() != null && !dto.getFinalAnswer().isEmpty())
                        ? dto.getFinalAnswer() : originalRecord.getContent();

                faqMapper.insertFaq(finalQuestion, finalAnswer);
            }

            // 4. 移除了无法执行的日志操作，直接返回结果
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}