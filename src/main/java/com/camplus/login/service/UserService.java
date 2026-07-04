package com.camplus.login.service;

import com.camplus.login.entity.User;

public interface UserService {

    boolean register(User user);

    User registerAndReturnUser(User user);

    User login(String loginAccount, String password);

    User updateUsername(Long userId, String newUsername);

    User updateEmail(Long userId, String newEmail);

    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    Integer getRole(Long userId);

    boolean isEmailExist(String email);
}
