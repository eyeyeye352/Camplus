package com.camplus.login.service;

public interface VerificationCodeService {

    String sendCode(String target, String type, String smtpPassword);

    boolean verifyCode(String target, String type, String code);
}