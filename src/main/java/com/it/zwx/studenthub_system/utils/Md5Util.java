package com.it.zwx.studenthub_system.utils;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Md5Util {

    public static String getMd5String(String input) {
        try {
            //MessageDigest.getInstance("MD5") 获取一个支持 MD5 算法的 MessageDigest 实例。
            MessageDigest md = MessageDigest.getInstance("MD5");
            //将输入字符串转换为字节数组，并计算其 MD5 摘要值，结果存储在 messageDigest 数组中。
            byte[] messageDigest = md.digest(input.getBytes());
            //将 messageDigest 字节数组转换为一个 BigInteger 对象。
            //这里的 1 表示是一个正数（符号位），messageDigest 是实际的字节数组。
            BigInteger no = new BigInteger(1, messageDigest);
            //将 BigInteger 对象转换为一个十六进制的字符串表示（即哈希值的十六进制形式）。
            String hashText = no.toString(16);
            //确保返回的哈希值是32位的字符串（MD5哈希值通常是128位二进制，即32位十六进制）。
            // 如果转换后的字符串长度不足32位，则在前面补零。
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
