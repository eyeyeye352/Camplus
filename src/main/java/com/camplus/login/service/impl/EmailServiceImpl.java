package com.camplus.login.service.impl;

import com.camplus.login.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${camplus.email.verification.template:您的验证码是：{code}，5分钟内有效。}")
    private String emailTemplate;

    @Override
    public boolean sendVerificationCode(String email, String code, String smtpPassword) {
        try {
            JavaMailSender mailSender = createMailSender(email, smtpPassword);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(email);
            message.setTo(email);
            message.setSubject("Camplus注册验证码");
            message.setText(emailTemplate.replace("{code}", code));
            mailSender.send(message);
            
            logger.info("验证码邮件发送成功: {}", email);
            return true;
        } catch (MailException e) {
            logger.error("验证码邮件发送失败: {}", email, e);
            return false;
        }
    }

    private JavaMailSender createMailSender(String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        String host = "smtp.qq.com";
        int port = 587;
        
        if (username != null) {
            if (username.endsWith("@gmail.com")) {
                host = "smtp.gmail.com";
                port = 587;
            } else if (username.endsWith("@163.com") || username.endsWith("@126.com")) {
                host = "smtp.163.com";
                port = 25;
            } else if (username.endsWith("@sina.com")) {
                host = "smtp.sina.com";
                port = 25;
            } else if (username.endsWith("@outlook.com") || username.endsWith("@hotmail.com")) {
                host = "smtp.office365.com";
                port = 587;
            } else if (username.endsWith("@yahoo.com")) {
                host = "smtp.mail.yahoo.com";
                port = 587;
            }
        }
        
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        
        return mailSender;
    }
}