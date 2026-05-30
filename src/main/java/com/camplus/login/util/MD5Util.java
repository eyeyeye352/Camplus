package com.camplus.login.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 密码加密工具
 */
public class MD5Util {

    /**
     * 明文密码 → MD5 加密
     */
    public static String md5Encrypt(String pwd) {
        if (pwd == null || "".equals(pwd)) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(pwd.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int num = b & 0xff;
                String str = Integer.toHexString(num);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}