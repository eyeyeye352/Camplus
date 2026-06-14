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
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam String password,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();
        User user = new User();
        user.setPasswordHash(password);

        if (username != null && !"".equals(username)) {
            user.setUsername(username);
        }
        if (email != null && !"".equals(email)) {
            user.setEmail(email);
        }
        if (phone != null && !"".equals(phone)) {
            user.setPhone(phone);
        }

        User registeredUser = userService.registerAndReturnUser(user);
        if (registeredUser != null) {
            request.getSession().setAttribute("user", registeredUser);
            result.put("success", true);
            result.put("msg", "注册成功！");
            result.put("userId", registeredUser.getUserId());
            result.put("username", registeredUser.getUsername());
            result.put("email", registeredUser.getEmail());
            result.put("phone", registeredUser.getPhone());
            result.put("nickname", registeredUser.getNickname());
            result.put("avatarUrl", registeredUser.getAvatarUrl());
            result.put("role", registeredUser.getRole());
            result.put("status", registeredUser.getStatus());
        } else {
            result.put("success", false);
            result.put("msg", "账号信息已存在，注册失败");
        }
        return result;
    }
}