package com.camplus.admin.service.impl;

import com.camplus.admin.Mappers.FaqItemMapper;
import com.camplus.admin.Mappers.UserContributionMapper;
import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
import com.camplus.admin.service.UserContributionService;
import com.camplus.login.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

public class UserContributionServiceImpl implements UserContributionService {

    /**
     * 1. 实现接口中的 getPendingList 方法
     * 用于在管理员后台首页加载所有未审核（status = 0）的用户贡献列表
     */
    @Override
    public List<UserContribution> getPendingList() {
        // 采用 try-with-resources 语法，执行完毕后会自动关闭 session
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            UserContributionMapper mapper = session.getMapper(UserContributionMapper.class);
            return mapper.selectPendingContributions();
        }
    }

    /**
     * 2. 实现接口中的 reviewContribution 方法
     * 处理管理员的审核操作，包含：修改内容、变更状态、数据入库、记录日志、事务回滚
     */
    @Override
    public boolean reviewContribution(ReviewRequestDTO dto, Long adminId, String ip) {
        SqlSession session = null;
        try {
            // 🌟 注意：这里不要使用 try-with-resources 自动关闭，因为我们需要在 catch 块中手动控制 rollback()
            session = MyBatisUtil.getSqlSession();
            UserContributionMapper contributionMapper = session.getMapper(UserContributionMapper.class);
            FaqItemMapper faqMapper = session.getMapper(FaqItemMapper.class);

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

                // 🌟 核心：确保以上所有数据库操作（写状态、写FAQ、写日志）都成功无误，才统一提交事务
                session.commit();
                return true;
            }
            return false;

        } catch (Exception e) {
            // 🌟 核心：一旦中途发生任何异常（如数据库连接断开、字段超长等），立刻回滚所有操作，避免产生脏数据
            if (session != null) {
                session.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close(); // 最终确保数据库连接被关闭并释放
            }
        }
    }
}