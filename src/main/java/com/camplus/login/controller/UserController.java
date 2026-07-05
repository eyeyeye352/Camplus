package com.camplus.login.controller;

import com.camplus.common.Result;
import com.camplus.login.entity.User;
import com.camplus.login.service.UserService;
import com.camplus.login.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    @Autowired
    public UserController(UserService userService, VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
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
        return Result.fail("账号或密码错误");
    }

    @PostMapping("/sendCode")
    public Result<String> sendCode(
            @RequestParam(name = "target") String target,
            @RequestParam(name = "type") String type,
            @RequestParam(name = "smtpPassword", required = false) String smtpPassword,
            @RequestParam(name = "scene", required = false, defaultValue = "register") String scene) {

        if (target == null || target.trim().isEmpty()) {
            return Result.fail("请输入目标账号");
        }

        if (!"email".equals(type)) {
            return Result.fail("验证码类型错误");
        }

        if (!EMAIL_PATTERN.matcher(target).matches()) {
            return Result.fail("邮箱格式不正确");
        }

        if ("register".equals(scene)) {
            if (userService.isEmailExist(target)) {
                return Result.fail("该邮箱已被注册");
            }
        } else if ("reset".equals(scene)) {
            if (!userService.isEmailExist(target)) {
                return Result.fail("该邮箱未注册");
            }
        }

        if (smtpPassword == null || smtpPassword.trim().isEmpty()) {
            return Result.fail("请输入SMTP授权码");
        }

        String code = verificationCodeService.sendCode(target, type, smtpPassword);
        if (code != null) {
            return Result.ok("验证码发送成功", code);
        }
        return Result.fail("验证码发送失败，请检查SMTP配置是否正确");
    }

    @PostMapping("/register")
    public Result<User> register(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "code", required = false) String code) {

        boolean needVerify = false;
        String verifyTarget = null;

        if (email != null && !email.isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return Result.fail("邮箱格式不正确");
            }
            needVerify = true;
            verifyTarget = email;
        } else {
            return Result.fail("请输入邮箱");
        }

        if (needVerify && (code == null || code.trim().isEmpty())) {
            return Result.fail("请输入验证码");
        }

        if (needVerify) {
            boolean verifySuccess = verificationCodeService.verifyCode(verifyTarget, "email", code);
            if (!verifySuccess) {
                return Result.fail("验证码错误或已过期");
            }
        }

        User user = new User();
        user.setPasswordHash(password);
        user.setEmail(email);

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

    @PostMapping("/user/delete")
    public Result<String> deleteUser(
            @RequestParam(name = "userId") Long userId) {
        try {
            boolean success = userService.deleteUser(userId);
            if (success) {
                return Result.ok("账号注销成功！", null);
            }
            return Result.fail("账号注销失败");
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/user/checkEmail")
    public Result<Map<String, Object>> checkEmail(
            @RequestParam(name = "email") String email) {
        Map<String, Object> data = new HashMap<>();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.fail("邮箱格式不正确");
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.fail("邮箱不存在");
        }
        data.put("isAdmin", "Administrator".equals(user.getUsername()));
        data.put("userId", user.getUserId());
        return Result.ok("验证成功", data);
    }

    @PostMapping("/user/resetPassword")
    public Result<String> resetPassword(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "code") String code,
            @RequestParam(name = "newPassword") String newPassword) {
        boolean success = userService.resetPassword(email, code, newPassword);
        if (success) {
            return Result.ok("密码重置成功！", null);
        }
        return Result.fail("密码重置失败，请检查验证码是否正确");
    }
}