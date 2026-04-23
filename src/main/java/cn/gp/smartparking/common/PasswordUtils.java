package cn.gp.smartparking.common;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码工具类
 */
public class PasswordUtils {

    /**
     * MD5加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        return DigestUtil.md5Hex(password);
    }

    /**
     * 验证密码
     * @param password 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String encryptedPassword) {
        return encryptPassword(password).equals(encryptedPassword);
    }
}