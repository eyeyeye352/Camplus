package com.camplus.contribution.dao;

import com.camplus.contribution.mappers.ContributionMapper;
import com.camplus.contribution.pojo.UserContribution;
import java.sql.SQLException;
import java.util.List;
import com.camplus.login.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

// 操作数据库
public class ContributionDao {

    // 插入数据
    public int insert(UserContribution contribution) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            mapper.insert(contribution);
            session.commit();
            return contribution.getContributionId() == null ? 0 : contribution.getContributionId();
        }
    }

    // 查询指定用户的贡献列表，可使用贡献 status 筛选
    public List<UserContribution> findByUserId(Integer userId, Integer status, int offset, int size)
            throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            return mapper.findByUserId(userId, status, offset, size);
        }
    }

    // 根据贡献 id 和用户 id 查询贡献详情
    public UserContribution findByIdAndUserId(Integer contributionId, Integer userId) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            return mapper.findByIdAndUserId(contributionId, userId);
        }
    }

    // 更新贡献内容(只能更新待审核和被拒绝的贡献)
    public boolean updatePending(UserContribution contribution) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            boolean success = mapper.updatePending(contribution) > 0;
            session.commit();
            return success;
        }
    }

    // 撤回待审核贡献
    public boolean deletePending(Integer contributionId, Integer userId) throws SQLException {
        try (SqlSession session = MyBatisUtil.getSqlSession()) {
            ContributionMapper mapper = session.getMapper(ContributionMapper.class);
            boolean success = mapper.deletePending(contributionId, userId) > 0;
            session.commit();
            return success;
        }
    }
}
