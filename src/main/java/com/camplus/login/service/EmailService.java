package com.camplus.login.service;

public interface EmailService {

    boolean sendVerificationCode(String email, String code, String smtpPassword);
}