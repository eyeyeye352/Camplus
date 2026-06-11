package com.camplus.contribution.mappers;

import com.camplus.contribution.pojo.UserContribution;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContributionMapper {

    int insert(UserContribution contribution);

    List<UserContribution> findByUserId(@Param("userId") Integer userId,
                                        @Param("status") Integer status,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    UserContribution findByIdAndUserId(@Param("contributionId") Integer contributionId,
                                       @Param("userId") Integer userId);

    int updatePending(UserContribution contribution);

    int deletePending(@Param("contributionId") Integer contributionId,
                      @Param("userId") Integer userId);
}
