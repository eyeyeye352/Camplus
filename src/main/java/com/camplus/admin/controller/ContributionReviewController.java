package com.camplus.admin.controller;

import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
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

    @PostMapping("/review")
    public Map<String, Object> doReview(@RequestBody ReviewRequestDTO requestDTO) {
        // 1. 移除拦截器已处理的身份验证代码，不从 Session 拿取任何数据
        // 2. 基础参数校验
        if (requestDTO.getContributionId() == null || requestDTO.getStatus() == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "参数不完整");
            return result;
        }

        // 3. 纯净调用业务层
        boolean isSuccess = contributionService.reviewContribution(requestDTO);

        Map<String, Object> result = new HashMap<>();
        if (isSuccess) {
            result.put("success", true);
            result.put("msg", "审核处理成功，数据已入库");
        } else {
            result.put("success", false);
            result.put("msg", "操作失败，可能数据状态已变更或发生异常");
        }
        return result;
    }
}