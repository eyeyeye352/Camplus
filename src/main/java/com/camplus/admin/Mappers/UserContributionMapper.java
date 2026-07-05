package com.camplus.admin.Mappers;

import com.camplus.admin.pojo.UserContribution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface UserContributionMapper {

    // 1. 联表查询所有待审核记录（users 表含 user_id 和 username，此 SQL 安全）
    @Select("SELECT c.*, u.username FROM user_contributions c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.status = 0 ORDER BY c.create_time DESC")
    List<UserContribution> selectPendingContributions();

    // 2. 仅更新当前表实际存在的 status 和 review_comment 字段
    @Update("UPDATE user_contributions SET status = #{status}, " +
            "review_comment = #{reviewComment} " +
            "WHERE contribution_id = #{contributionId}")
    int updateReviewData(@Param("contributionId") Integer contributionId,
                         @Param("status") Integer status,
                         @Param("reviewComment") String reviewComment);

    @Select("SELECT * FROM user_contributions WHERE contribution_id = #{contributionId}")
    UserContribution selectById(@Param("contributionId") Integer contributionId);

    @Select("SELECT c.*, u.username FROM user_contributions c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "ORDER BY c.create_time DESC")
    List<UserContribution> selectAllContributions();
}