package com.camplus.admin.Mappers;

import com.camplus.admin.pojo.UserContribution;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface UserContributionMapper {

    // 1. 联表查询所有待审核记录
    @Select("SELECT c.*, u.username FROM user_contributions c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.status = 0 ORDER BY c.create_time DESC")
    List<UserContribution> selectPendingContributions();

    // 2. 更新审核状态与评语
    @Update("UPDATE user_contributions SET status = #{status}, review_user_id = #{reviewUserId}, " +
            "review_comment = #{reviewComment}, question = #{finalQuestion}, answer = #{finalAnswer}, " +
            "content = #{finalContent}, source_url = #{finalSourceUrl}, review_time = CURRENT_TIMESTAMP, " +
            "update_time = CURRENT_TIMESTAMP WHERE contribution_id = #{contributionId}")
    int updateReviewData(@Param("contributionId") Long contributionId,
                         @Param("reviewUserId") Long reviewUserId,
                         @Param("status") Integer status,
                         @Param("reviewComment") String reviewComment,
                         @Param("finalQuestion") String finalQuestion,
                         @Param("finalAnswer") String finalAnswer,
                         @Param("finalContent") String finalContent,
                         @Param("finalSourceUrl") String finalSourceUrl);

    // 3. 联动插入管理员审计日志
    @Insert("INSERT INTO admin_operation_logs (user_id, action, target_table, target_id, detail, ip) " +
            "VALUES (#{adminId}, #{action}, #{targetTable}, #{targetId}, #{detail}, #{ip})")
    int insertAdminLog(@Param("adminId") Long adminId,
                       @Param("action") String action,
                       @Param("targetTable") String targetTable,
                       @Param("targetId") Long targetId,
                       @Param("detail") String detail,
                       @Param("ip") String ip);

    @Select("SELECT * FROM user_contributions WHERE contribution_id = #{contributionId}")
    UserContribution selectById(@Param("contributionId") Long contributionId);
}
