package com.camplus.login.controller;

import com.camplus.login.entity.User;
import com.camplus.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String loginAccount,
            @RequestParam String password,
            @RequestParam(required = false) String rememberMe,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();
        User user = userService.login(loginAccount, password);

        if (user != null) {
            request.getSession().setAttribute("user", user);
            result.put("success", true);
            result.put("msg", "登录成功！");
            result.put("userId", user.getUserId());
            result.put("username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("nickname", user.getNickname());
            result.put("avatarUrl", user.getAvatarUrl());
            result.put("role", user.getRole());
            result.put("status", user.getStatus());
        } else {
            result.put("success", false);
            result.put("msg", "账号或密码错误，或账号已被锁定/禁用");
        }
        return result;
    }
}