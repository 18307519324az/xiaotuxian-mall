package com.xtx.common.security.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密工具类。
 * 基于 BCrypt 算法对用户密码进行哈希加密和校验。
 * BCrypt 自动加入随机盐，相同明文每次加密结果不同，安全性更高。
 */
public class PasswordUtil {

    /**
     * 对原始密码进行 BCrypt 加密。
     *
     * @param rawPassword 原始明文密码
     * @return 加密后的哈希字符串
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }

    /**
     * 校验原始密码是否与加密后的哈希值匹配。
     *
     * @param rawPassword     原始明文密码
     * @param encodedPassword 加密后的哈希字符串
     * @return true 匹配，false 不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
