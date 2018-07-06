package com.jsdroid.core;

import java.io.File;
import java.io.InputStream;

import com.jsdroid.util.FileUtil;
import com.jsdroid.util.HttpUtil;

public class JsInclude {
	public static byte[] readBytes(String url, String scriptDir) {
		byte[] data = null;
		data = FileUtil.readBytes(url);
		if (data == null || data.length == 0) {
			// 从包中的script文件夹加载
			data = readBytesFromResouce(url);
		}
		if (data == null || data.length == 0) {
			// 从同文件夹中加载
			File file = new File(scriptDir, url);
			data = FileUtil.readBytes(file);
			if (data == null || data.length == 0) {
				// 从同文件夹的包中加载
				data = readBytesFromResouce(file.getPath());
			}
		}
		return data;
	}

	public static String readSource(String url, String scriptDir) {
		String source = null;
		if (url.startsWith("http")) {
			try {
				source = HttpUtil.get(url, null);
				return source;
			} catch (Exception e) {
				return null;
			}
		}
		if (new File(url).exists()) {
			source = FileUtil.read(url);
			return source;
		} else if (new File(scriptDir, url).exists()) {
			source = FileUtil.read(new File(scriptDir, url));
			return source;
		}
		// 从包中加载
		source = readSourceFromResouce(url);
		if (source == null || source.trim().length() == 0) {
			return null;
		}
		return source;
	}

	public static byte[] readBytesFromResouce(String file) {
		InputStream input = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(file);
		return FileUtil.readBytes(input);
	}

	public static String readSourceFromResouce(String file) {
		InputStream input = JsInclude.class.getClassLoader()
				.getResourceAsStream(file);
		return FileUtil.read(input);
	}
}
