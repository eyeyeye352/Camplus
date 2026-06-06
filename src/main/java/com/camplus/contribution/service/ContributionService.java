package com.camplus.contribution.service;

import com.camplus.contribution.dao.ContributionDao;
import com.camplus.contribution.pojo.UserContribution;

import java.sql.SQLException;
import java.util.List;

public class ContributionService {
//    表示待审核状态
    private static final int STATUS_PENDING = 0;
    private final ContributionDao contributionDao = new ContributionDao();

//     创建用户贡献
    public int create(UserContribution contribution, Integer currentUserId) throws SQLException {
//        确认登录状态
        ensureLoggedIn(currentUserId);
        contribution.setUserId(currentUserId);
        contribution.setStatus(STATUS_PENDING);
//        检查贡献内容是否合法
        validate(contribution);
        return contributionDao.insert(contribution);
    }

//    查询用户贡献
    public List<UserContribution> listMine(Integer currentUserId, Integer status, int page, int pageSize)
            throws SQLException {
        ensureLoggedIn(currentUserId);
        if (status != null && (status < 0 || status > 2)) {
            throw new IllegalArgumentException("审核状态不正确");
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (safePage - 1) * safeSize;
        return contributionDao.findByUserId(currentUserId, status, offset, safeSize);
    }

//    查询执行用户贡献详情
    public UserContribution detail(Integer contributionId, Integer currentUserId) throws SQLException {
        ensureLoggedIn(currentUserId);
        requireId(contributionId, "贡献ID不能为空");
        UserContribution contribution = contributionDao.findByIdAndUserId(contributionId, currentUserId);
        if (contribution == null) {
            throw new IllegalArgumentException("贡献记录不存在或无权访问");
        }
        return contribution;
    }

//    更新自己的贡献
    public void update(UserContribution contribution, Integer currentUserId) throws SQLException {
        ensureLoggedIn(currentUserId);
        requireId(contribution.getContributionId(), "贡献ID不能为空");
        contribution.setUserId(currentUserId);
        validate(contribution);
        if (!contributionDao.updatePending(contribution)) {
            throw new IllegalArgumentException("只能修改自己的待审核贡献或者已拒绝贡献");
        }
    }

//    撤回自己的待审核贡献
    public void delete(Integer contributionId, Integer currentUserId) throws SQLException {
        ensureLoggedIn(currentUserId);
        requireId(contributionId, "贡献ID不能为空");
        if (!contributionDao.deletePending(contributionId, currentUserId)) {
            throw new IllegalArgumentException("只能撤回自己的待审核贡献");
        }
    }

//    检验贡献内容是否合法
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

//    检验当前用户是否是登录状态
    private void ensureLoggedIn(Integer currentUserId) {
//        根据session检验...
        if (currentUserId == null) {
            throw new SecurityException("请先登录");
        }
    }

    private void requireId(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

//    判断字符串是否为空
    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
