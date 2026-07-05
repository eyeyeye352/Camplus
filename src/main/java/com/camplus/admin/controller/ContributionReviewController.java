package com.camplus.admin.controller;

import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.contribution.pojo.UserContribution;
import com.camplus.admin.service.UserContributionService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/contribution")
public class ContributionReviewController {
    private final UserContributionService contributionService;

    public ContributionReviewController(UserContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @GetMapping("/list")
    public List<UserContribution> getPendingList() {
        return contributionService.getPendingList();
    }

    @GetMapping("/all")
    public List<UserContribution> getAllList() {
        return contributionService.getAllContributions();
    }

    @PostMapping("/review")
    public Map<String, Object> doReview(@RequestBody ReviewRequestDTO requestDTO) {
        if (requestDTO.getContributionId() == null || requestDTO.getStatus() == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "参数不完整");
            return result;
        }

        boolean isSuccess = contributionService.reviewContribution(requestDTO);

        Map<String, Object> result = new HashMap<>();
        if (isSuccess) {
            result.put("success", true);
            result.put("msg", "审核处理成功");
        } else {
            result.put("success", false);
            result.put("msg", "操作失败，可能数据状态已变更或发生异常");
        }
        return result;
    }
}