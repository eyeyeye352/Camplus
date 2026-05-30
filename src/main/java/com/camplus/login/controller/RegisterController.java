package com.camplus.login.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RegisterController {

    @PostMapping("/register")
    public Map<String, Object> register(
            @RequestParam String password,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String username) {

        System.out.println("===== 注册接口 =====");
        System.out.println("邮箱：" + email);
        System.out.println("手机号：" + phone);
        System.out.println("用户名：" + username);
        System.out.println("密码：" + password);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("msg", "注册接口连通成功");
        return result;
    }
}