package com.camplus.contribution.mappers;

import com.camplus.contribution.pojo.UserContribution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户贡献Mapper接口
 * 提供用户贡献数据的增删改查功能
 */
@Mapper
public interface ContributionMapper {

    int insert(UserContribution contribution);

    List<UserContribution> findByUserId(@Param("userId") Long userId,
                                        @Param("status") Integer status,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    int countByUserId(@Param("userId") Long userId,
                      @Param("status") Integer status);

    UserContribution findByIdAndUserId(@Param("contributionId") Integer contributionId,
                                       @Param("userId") Long userId);

    int updatePending(UserContribution contribution);

    int deletePending(@Param("contributionId") Integer contributionId,
                      @Param("userId") Long userId);

    List<UserContribution> selectPendingContributions();

    List<UserContribution> selectAllContributions();

    UserContribution selectById(@Param("contributionId") Integer contributionId);

    int updateReviewData(@Param("contributionId") Integer contributionId,
                         @Param("status") Integer status,
                         @Param("reviewComment") String reviewComment);
}
