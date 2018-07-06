package com.jsdroid.util;

import java.util.Random;

public class RandomUtil {
	static Random random = new Random();
	public static byte[] randomBytes(int len){
		byte[] bytes = new byte[len];
		random.nextBytes(bytes);
		return bytes;
	}
}
