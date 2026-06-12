package com.camplus.contribution.dao;

import com.camplus.contribution.mappers.ContributionMapper;
import com.camplus.contribution.pojo.UserContribution;
import com.camplus.login.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;
import java.util.List;

public class ContributionDao {

    public int insert(UserContribution contribution) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            mapper.insert(contribution);
            session.commit();
            return contribution.getContributionId() == null ? 0 : contribution.getContributionId();
        }
    }

    public List<UserContribution> findByUserId(Integer userId, Integer status, int offset, int size)
            throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            return mapper.findByUserId(userId, status, offset, size);
        }
    }

    public UserContribution findByIdAndUserId(Integer contributionId, Integer userId) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            return mapper.findByIdAndUserId(contributionId, userId);
        }
    }

    public boolean updatePending(UserContribution contribution) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            boolean success = mapper.updatePending(contribution) > 0;
            session.commit();
            return success;
        }
    }

    public boolean deletePending(Integer contributionId, Integer userId) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            boolean success = mapper.deletePending(contributionId, userId) > 0;
            session.commit();
            return success;
        }
    }
}
