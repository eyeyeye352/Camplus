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

    /**
     * 1. 实现接口中的 getPendingList 方法
     * 用于在管理员后台首页加载所有未审核（status = 0）的用户贡献列表
     */
    @Override
    public List<UserContribution> getPendingList() {
        return contributionMapper.selectPendingContributions();
    }

    /**
     * 2. 实现接口中的 reviewContribution 方法
     * 处理管理员的审核操作，包含：修改内容、变更状态、数据入库、记录日志、事务回滚
     */
    @Override
    @Transactional
    public boolean reviewContribution(ReviewRequestDTO dto, Long adminId, String ip) {
        try {
            // 1. 获取原始贡献记录，校验是否存在以及是否为待审核状态
            // (请确保你的 UserContributionMapper 接口中已经声明了 selectById 方法)
            UserContribution originalRecord = contributionMapper.selectById(dto.getContributionId());
            if (originalRecord == null || originalRecord.getStatus() != 0) {
                return false;
            }

            // 2. 无论通过还是拒绝，都更新贡献表记录（保存管理员可能修改后的最终内容以及评语）
            int rows = contributionMapper.updateReviewData(
                    dto.getContributionId(), adminId, dto.getStatus(), dto.getComment(),
                    dto.getFinalQuestion(), dto.getFinalAnswer(), dto.getFinalContent(), dto.getFinalSourceUrl()
            );

            // 3. 触发联动事件：如果审核结果为“通过 (status == 1)”，则执行正式的数据入库操作
            if (rows > 0 && dto.getStatus() == 1) {
                // 根据贡献类型 (0:新增问题, 1:答案纠错) 决定去向
                if (originalRecord.getContributionType() == 0 || originalRecord.getContributionType() == 1) {
                    // 默认分类设置为 1L（防止前端没传分类导致报错），并保留原贡献用户的 userId 作为激励数据
                    Long categoryId = dto.getCategoryId() != null ? dto.getCategoryId() : 1L;
                    faqMapper.insertFaq(categoryId, originalRecord.getUserId(), dto.getFinalQuestion(), dto.getFinalAnswer());
                }
                // 如果后续有资料库贡献 (type == 2)，可以在这里扩展 knowledge_docs 的入库 Mapper
            }

            // 4. 联动操作：只要状态成功更新，就写入管理员操作审计日志
            if (rows > 0) {
                String detail = "操作结果: " + (dto.getStatus() == 1 ? "通过入库" : "驳回") + " | 审批意见: " + dto.getComment();
                contributionMapper.insertAdminLog(adminId, "审核贡献", "user_contributions", dto.getContributionId(), detail, ip);

                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
