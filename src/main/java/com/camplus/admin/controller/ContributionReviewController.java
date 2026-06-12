package com.camplus.admin.controller;

import com.camplus.admin.pojo.ReviewRequestDTO;
import com.camplus.admin.pojo.UserContribution;
import com.camplus.admin.service.UserContributionService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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

    // 前端发送 GET 请求到 /admin/contribution/list 加载未审核数据
    @GetMapping("/list")
    public List<UserContribution> getPendingList() {
        // Spring Boot 会自动利用内置的 Jackson 工具把 List 转换为前端需要的 JSON 数组返回
        return contributionService.getPendingList();
    }

    // 前端管理员点击按钮，发送 POST 请求到 /admin/contribution/review
    @PostMapping("/review")
    public Map<String, Object> doReview(@RequestBody ReviewRequestDTO requestDTO, HttpServletRequest request) {

        // 测试阶段先固定为1，后续接了登录模块后，这里换成从 Session 拿
        Long adminId = 1L;
        String ip = request.getRemoteAddr();

        // 基础参数校验
        if (requestDTO.getContributionId() == null || requestDTO.getStatus() == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "参数不完整");
            return result;
        }

        // 调用 Service
        boolean isSuccess = contributionService.reviewContribution(requestDTO, adminId, ip);

        Map<String, Object> result = new HashMap<>();
        if (isSuccess) {
            result.put("success", true);
            result.put("msg", "审核处理成功，数据已联动更新");
        } else {
            result.put("success", false);
            result.put("msg", "操作失败，可能数据不存在或服务器异常");
        }
        return result;
    }
}
