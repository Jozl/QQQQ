package com;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class Cryptography {
	public static String getHash(String plainText, String hashType) {
		try {
			MessageDigest md = MessageDigest.getInstance(hashType);// 算法
			byte[] encryptStr = md.digest(plainText.getBytes("UTF-8"));// 摘要
			return DatatypeConverter.printHexBinary(encryptStr); // 16进制字符�?
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			return null;
		}
	}

	public static SecretKey generateNewKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128); // 128,192,256
			SecretKey secretKey = keyGenerator.generateKey();// 新密�?
			return secretKey;
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	private Cryptography() {
	}
}// end class
