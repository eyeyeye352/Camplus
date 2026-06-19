package com.camplus.login.controller;

import com.camplus.common.Result;
import com.camplus.login.entity.User;
import com.camplus.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<User> login(
            @RequestParam String loginAccount,
            @RequestParam String password,
            @RequestParam(required = false) String rememberMe
    ) {

        User user = userService.login(loginAccount, password);

        if (user != null) {
            return Result.ok("登录成功！", user);
        }
        return Result.fail("账号或密码错误，或账号已被锁定/禁用");
    }
}
