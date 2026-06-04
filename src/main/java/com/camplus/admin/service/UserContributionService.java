package com.camplus.admin.service;

import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
import java.util.List;

public interface UserContributionService {
    // 1. 获取待审核列表
    List<UserContribution> getPendingList();

    // 2. 审核贡献（参数改用接收封装好的 DTO 对象）
    boolean reviewContribution(ReviewRequestDTO dto, Long adminId, String ip);
}