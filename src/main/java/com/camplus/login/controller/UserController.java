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

    @PostMapping("/user/updateUsername")
    public Result<User> updateUsername(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "newUsername") String newUsername) {
        User user = userService.updateUsername(userId, newUsername);
        if (user != null) {
            return Result.ok("用户名修改成功！", user);
        }
        return Result.fail("用户名已存在，修改失败");
    }

    @PostMapping("/user/updateEmail")
    public Result<User> updateEmail(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "newEmail") String newEmail) {
        User user = userService.updateEmail(userId, newEmail);
        if (user != null) {
            return Result.ok("邮箱修改成功！", user);
        }
        return Result.fail("邮箱已存在，修改失败");
    }

    @PostMapping("/user/updatePhone")
    public Result<User> updatePhone(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "newPhone") String newPhone) {
        User user = userService.updatePhone(userId, newPhone);
        if (user != null) {
            return Result.ok("手机号修改成功！", user);
        }
        return Result.fail("手机号已存在，修改失败");
    }

    @PostMapping("/user/updatePassword")
    public Result<String> updatePassword(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "oldPassword") String oldPassword,
            @RequestParam(name = "newPassword") String newPassword) {
        boolean success = userService.updatePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.ok("密码修改成功！", null);
        }
        return Result.fail("原密码错误，修改失败");
    }

    @PostMapping("/user/getRole")
    public Result<Integer> getRole(
            @RequestParam(name = "userId") Long userId) {
        Integer role = userService.getRole(userId);
        if (role != null) {
            return Result.ok("查询成功", role);
        }
        return Result.fail("用户不存在");
    }
}
