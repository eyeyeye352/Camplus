package com.camplus.admin.controller;

import com.camplus.login.entity.User;
import com.camplus.login.mappers.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserManagementController {

    private final UserMapper userMapper;

    public UserManagementController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/listAdmin")
    public Map<String, Object> listAdminUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> adminList = userMapper.selectByRole(1);
            result.put("success", true);
            result.put("data", adminList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/addAdmin")
    public Map<String, Object> addAdmin(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("msg", "用户名不能为空");
            return result;
        }

        try {
            User user = userMapper.selectByUsername(username.trim());
            if (user == null) {
                result.put("success", false);
                result.put("msg", "用户不存在");
                return result;
            }

            if (user.getRole() != null && user.getRole() == 1) {
                result.put("success", false);
                result.put("msg", "该用户已是管理员");
                return result;
            }

            int rows = userMapper.updateRole(user.getUserId(), 1);
            if (rows > 0) {
                result.put("success", true);
                result.put("msg", "设置成功");
            } else {
                result.put("success", false);
                result.put("msg", "操作失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", "操作失败: " + e.getMessage());
        }
        return result;
    }
}