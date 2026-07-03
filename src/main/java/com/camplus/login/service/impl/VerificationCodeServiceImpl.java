package com.camplus.login.service.impl;

import com.camplus.login.entity.VerificationCode;
import com.camplus.login.mappers.VerificationCodeMapper;
import com.camplus.login.service.EmailService;
import com.camplus.login.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);

    @Autowired
    private VerificationCodeMapper codeMapper;

    @Autowired
    private EmailService emailService;

    @Value("${camplus.verification.code.length:6}")
    private int codeLength;

    @Value("${camplus.verification.code.expire-minutes:5}")
    private int expireMinutes;

    @Value("${camplus.verification.code.max-error-count:3}")
    private int maxErrorCount;

    private static final String TYPE_EMAIL = "email";
    private static final int STATUS_UNUSED = 0;
    private static final int STATUS_USED = 1;
    private static final int STATUS_INVALID = 2;

    @Override
    @Transactional
    public String sendCode(String target, String type, String smtpPassword) {
        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(expireMinutes);

        VerificationCode existCode = codeMapper.selectByTargetAndType(target, type);
        if (existCode != null) {
            codeMapper.updateCode(existCode.getId(), code, expireTime);
            codeMapper.updateStatus(existCode.getId(), STATUS_UNUSED);
            codeMapper.updateErrorCount(existCode.getId(), 0);
        } else {
            VerificationCode newCode = new VerificationCode();
            newCode.setTarget(target);
            newCode.setCode(code);
            newCode.setType(type);
            newCode.setStatus(STATUS_UNUSED);
            newCode.setErrorCount(0);
            newCode.setExpireTime(expireTime);
            codeMapper.insertCode(newCode);
        }

        boolean sendSuccess = false;
        if (TYPE_EMAIL.equals(type)) {
            sendSuccess = emailService.sendVerificationCode(target, code, smtpPassword);
        }

        if (sendSuccess) {
            logger.info("验证码发送成功, target: {}, type: {}", target, type);
            return code;
        } else {
            logger.error("验证码发送失败, target: {}, type: {}", target, type);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean verifyCode(String target, String type, String code) {
        VerificationCode existCode = codeMapper.selectByTargetAndType(target, type);
        if (existCode == null) {
            logger.warn("验证码不存在, target: {}, type: {}", target, type);
            return false;
        }

        if (existCode.getStatus() == STATUS_USED) {
            logger.warn("验证码已使用, target: {}, type: {}", target, type);
            return false;
        }

        if (existCode.getStatus() == STATUS_INVALID) {
            logger.warn("验证码已失效, target: {}, type: {}", target, type);
            return false;
        }

        if (existCode.getExpireTime().isBefore(LocalDateTime.now())) {
            codeMapper.updateStatus(existCode.getId(), STATUS_INVALID);
            logger.warn("验证码已过期, target: {}, type: {}", target, type);
            return false;
        }

        if (existCode.getErrorCount() >= maxErrorCount) {
            codeMapper.updateStatus(existCode.getId(), STATUS_INVALID);
            logger.warn("验证码错误次数过多, target: {}, type: {}", target, type);
            return false;
        }

        if (!existCode.getCode().equals(code)) {
            int newErrorCount = existCode.getErrorCount() + 1;
            codeMapper.updateErrorCount(existCode.getId(), newErrorCount);
            if (newErrorCount >= maxErrorCount) {
                codeMapper.updateStatus(existCode.getId(), STATUS_INVALID);
            }
            logger.warn("验证码错误, target: {}, type: {}, errorCount: {}", target, type, newErrorCount);
            return false;
        }

        codeMapper.updateStatus(existCode.getId(), STATUS_USED);
        logger.info("验证码验证成功, target: {}, type: {}", target, type);
        return true;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < codeLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanExpiredCodes() {
        try {
            codeMapper.deleteExpiredCodes(LocalDateTime.now());
            logger.info("定时清理过期验证码完成");
        } catch (Exception e) {
            logger.warn("定时清理过期验证码失败，数据库连接异常: {}", e.getMessage());
        }
    }
}