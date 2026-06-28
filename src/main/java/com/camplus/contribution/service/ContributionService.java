package com.camplus.contribution.service;

import com.camplus.contribution.dao.ContributionDao;
import com.camplus.contribution.pojo.ContributionPage;
import com.camplus.contribution.pojo.UserContribution;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ContributionService {
    private static final int STATUS_PENDING = 0;
    private final ContributionDao contributionDao;

    public ContributionService(ContributionDao contributionDao) {
        this.contributionDao = contributionDao;
    }

    public int create(UserContribution contribution, Integer userId) throws SQLException {
        ensureUserId(userId);
        contribution.setUserId(userId);
        contribution.setStatus(STATUS_PENDING);
        validate(contribution);
        return contributionDao.insert(contribution);
    }

    public ContributionPage listMine(Integer userId, Integer status, int page, int pageSize)
            throws SQLException {
        ensureUserId(userId);
        if (status != null && (status < 0 || status > 2)) {
            throw new IllegalArgumentException("审核状态不正确");
        }
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int total = contributionDao.countByUserId(userId, status);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / safeSize));
        int safePage = Math.min(Math.max(page, 1), totalPages);
        int offset = (safePage - 1) * safeSize;
        List<UserContribution> items = contributionDao.findByUserId(userId, status, offset, safeSize);
        return new ContributionPage(items, safePage, safeSize, total, totalPages);
    }

    public UserContribution detail(Integer contributionId, Integer userId) throws SQLException {
        ensureUserId(userId);
        requireId(contributionId, "贡献ID不能为空");
        UserContribution contribution = contributionDao.findByIdAndUserId(contributionId, userId);
        if (contribution == null) {
            throw new IllegalArgumentException("贡献记录不存在或无权访问");
        }
        return contribution;
    }

    public void update(UserContribution contribution, Integer userId) throws SQLException {
        ensureUserId(userId);
        requireId(contribution.getContributionId(), "贡献ID不能为空");
        contribution.setUserId(userId);
        validate(contribution);
        if (!contributionDao.updatePending(contribution)) {
            throw new IllegalArgumentException("只能修改自己的待审核贡献或者已拒绝贡献");
        }
    }

    public void delete(Integer contributionId, Integer userId) throws SQLException {
        ensureUserId(userId);
        requireId(contributionId, "贡献ID不能为空");
        if (!contributionDao.deletePending(contributionId, userId)) {
            throw new IllegalArgumentException("只能撤回自己的待审核贡献");
        }
    }

    private void validate(UserContribution contribution) {
        Integer type = contribution.getContributionType();
        if (type == null || type < 0 || type > 1) {
            throw new IllegalArgumentException("贡献类型不正确");
        }
        if (blank(contribution.getTitle())) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (contribution.getTitle().length() > 128) {
            throw new IllegalArgumentException("标题不能超过128个字符");
        }
        if (blank(contribution.getContent())) {
            throw new IllegalArgumentException("贡献内容不能为空");
        }
    }

    private void ensureUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new SecurityException("请提供用户ID");
        }
    }

    private void requireId(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
