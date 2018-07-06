package com.jsdroid.util;


public class ByteUtil {
	public static byte[] stringToBytes(String text, String charset)
			throws Exception {
		return text.getBytes(charset);
	}
}
