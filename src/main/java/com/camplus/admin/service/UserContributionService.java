package com.camplus.admin.service;

import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
import java.util.List;

public interface UserContributionService {
    List<UserContribution> getPendingList();

    // 移除了无处安放的 adminId 和 ip 参数
    boolean reviewContribution(ReviewRequestDTO dto);
}