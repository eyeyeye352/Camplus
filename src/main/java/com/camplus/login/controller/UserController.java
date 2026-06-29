package com.camplus.login.controller;

import com.camplus.common.Result;
import com.camplus.login.entity.User;
import com.camplus.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<User> login(
            @RequestParam(name = "loginAccount") String loginAccount,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "rememberMe", required = false) String rememberMe) {

        User user = userService.login(loginAccount, password);

        if (user != null) {
            return Result.ok("登录成功！", user);
        }
        return Result.fail("账号或密码错误，或账号已被锁定/禁用");
    }

    @PostMapping("/register")
    public Result<User> register(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "password") String password) {

        User user = new User();
        user.setPasswordHash(password);

        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }

        User registeredUser = userService.registerAndReturnUser(user);

        if (registeredUser != null) {
            return Result.ok("注册成功！", registeredUser);
        }
        return Result.fail("账号信息已存在，注册失败");
    }
}
