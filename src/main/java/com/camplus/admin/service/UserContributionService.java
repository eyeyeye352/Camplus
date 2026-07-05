package com.camplus.admin.service;

import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.contribution.pojo.UserContribution;
import java.util.List;

public interface UserContributionService {
    List<UserContribution> getPendingList();

    List<UserContribution> getAllContributions();

    boolean reviewContribution(ReviewRequestDTO dto);
}