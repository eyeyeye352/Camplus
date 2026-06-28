package com.camplus.contribution.dao;

import com.camplus.contribution.mappers.ContributionMapper;
import com.camplus.contribution.pojo.UserContribution;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class ContributionDao {
    private final ContributionMapper contributionMapper;

    public ContributionDao(ContributionMapper contributionMapper) {
        this.contributionMapper = contributionMapper;
    }

    public int insert(UserContribution contribution) throws SQLException {
        contributionMapper.insert(contribution);
        return contribution.getContributionId() == null ? 0 : contribution.getContributionId();
    }

    public List<UserContribution> findByUserId(Integer userId, Integer status, int offset, int size)
            throws SQLException {
        return contributionMapper.findByUserId(userId, status, offset, size);
    }

    public int countByUserId(Integer userId, Integer status) throws SQLException {
        return contributionMapper.countByUserId(userId, status);
    }

    public UserContribution findByIdAndUserId(Integer contributionId, Integer userId) throws SQLException {
        return contributionMapper.findByIdAndUserId(contributionId, userId);
    }

    public boolean updatePending(UserContribution contribution) throws SQLException {
        return contributionMapper.updatePending(contribution) > 0;
    }

    public boolean deletePending(Integer contributionId, Integer userId) throws SQLException {
        return contributionMapper.deletePending(contributionId, userId) > 0;
    }
}
