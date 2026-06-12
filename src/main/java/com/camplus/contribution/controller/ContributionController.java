package com.camplus.contribution.controller;

import com.camplus.contribution.pojo.UserContribution;
import com.camplus.contribution.service.ContributionService;
import com.camplus.login.entity.User;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contribution")
public class ContributionController {
    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @GetMapping("/current-user")
    public Map<String, Object> currentUser(HttpSession session) {
        User user = requireCurrentUser(session);
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        return success("查询成功", userInfo);
    }

    @PostMapping("/create")
    public Map<String, Object> create(
            HttpSession session,
            @RequestParam(name = "contribution_type", required = false) Integer contributionType,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content) throws SQLException {
        UserContribution contribution = fromRequest(contributionType, title, content);
        int contributionId = contributionService.create(contribution, currentUserId(session));
        return success("提交成功", Map.of("contributionId", contributionId));
    }

    @GetMapping("/list")
    public Map<String, Object> list(
            HttpSession session,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) throws SQLException {
        List<UserContribution> contributions = contributionService.listMine(currentUserId(session), status, page, pageSize);
        return success("查询成功", contributions);
    }

    @GetMapping("/detail")
    public Map<String, Object> detail(
            HttpSession session,
            @RequestParam(name = "contribution_id", required = false) Integer contributionId) throws SQLException {
        UserContribution contribution = contributionService.detail(contributionId, currentUserId(session));
        return success("查询成功", contribution);
    }

    @PostMapping("/update")
    public Map<String, Object> update(
            HttpSession session,
            @RequestParam(name = "contribution_id", required = false) Integer contributionId,
            @RequestParam(name = "contribution_type", required = false) Integer contributionType,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content) throws SQLException {
        UserContribution contribution = fromRequest(contributionType, title, content);
        contribution.setContributionId(contributionId);
        contributionService.update(contribution, currentUserId(session));
        return success("修改成功", null);
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(
            HttpSession session,
            @RequestParam(name = "contribution_id", required = false) Integer contributionId) throws SQLException {
        contributionService.delete(contributionId, currentUserId(session));
        return success("撤回成功", null);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(exception.getMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("数据库操作失败"));
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String, Object>> handlePersistenceException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("数据库操作失败"));
    }

    private UserContribution fromRequest(Integer contributionType, String title, String content) {
        UserContribution contribution = new UserContribution();
        contribution.setContributionType(contributionType);
        contribution.setTitle(trim(title));
        contribution.setContent(trim(content));
        return contribution;
    }

    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("data", null);
        return result;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Integer currentUserId(HttpSession session) {
        Long userId = requireCurrentUser(session).getUserId();
        if (userId == null || userId <= 0) {
            throw new SecurityException("请先登录");
        }
        return Math.toIntExact(userId);
    }

    private User requireCurrentUser(HttpSession session) {
        Object user = session.getAttribute("user");
        if (!(user instanceof User)) {
            throw new SecurityException("请先登录");
        }
        return (User) user;
    }
}
