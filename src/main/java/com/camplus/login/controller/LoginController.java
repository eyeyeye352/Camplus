package com.camplus.login.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String loginAccount,
            @RequestParam String password,
            @RequestParam(required = false) String rememberMe) {

        // 控制台打印参数，测试连通性
        System.out.println("===== 登录接口 =====");
        System.out.println("账号：" + loginAccount);
        System.out.println("密码：" + password);
        System.out.println("记住我：" + rememberMe);

        // 返回JSON结果
        Map<String, Object> json = new HashMap<>();
        json.put("success", true);
        json.put("msg", "后端连通成功，参数已收到");
        return json;
    }
}