package com;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.bouncycastle.util.encoders.Hex;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import db.beans.User;

public class Translater {
	private static Gson gson = new Gson();

	public static String fromMessageToJson(Message<?> message) {
		return new Gson().toJson(message);
	}

	public static <T> Message<?> fromJsonToMessage(String Json) {
		Message<?> result = null;
		try {
			result = gson.fromJson(Json, new TypeToken<Message<User>>() {
			}.getType());
		} catch (JsonSyntaxException e1) {
			try {
				result = gson.fromJson(Json, new TypeToken<Message<List<User>>>() {
				}.getType());
			} catch (JsonSyntaxException e2) {
				try {
					result = gson.fromJson(Json, new TypeToken<Message<String>>() {
					}.getType());
				} catch (JsonSyntaxException e3) {
					try {
						result = gson.fromJson(Json, new TypeToken<Message<List<String>>>() {
						}.getType());
					} catch (JsonSyntaxException e4) {
						try {
							result = gson.fromJson(Json, new TypeToken<Message<Integer>>() {
							}.getType());
						} catch (JsonSyntaxException e5) {
							// (;∀;)
						}
					}
				}
			}
		}
		return result;
	}

	public static String cryptWithSHA256(String string) {
		MessageDigest messageDigest;
		String result = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(string.getBytes("UTF-8"));
			result = Hex.toHexString(messageDigest.digest(string.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String fromBufferedImageToBase64(BufferedImage image, String type) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, type, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Base64.getEncoder().encodeToString(output.toByteArray());
	}

	public static BufferedImage fromBase64ToBufferedImage(String imageBase64) {
		byte[] input = Base64.getDecoder().decode(imageBase64);
		try {
			return ImageIO.read(new ByteArrayInputStream(input));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
