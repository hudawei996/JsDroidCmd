package com.jsdroid.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceUtil {
	public static final String CPU_ABIS[] = new String[] { "arm64-v8a",
			"armeabi", "armeabi-v7a", "mips", "mips64", "x86", "x86_64" };

	public static void releaseFile(String resName, String destFile)
			throws IOException {
		InputStream in = ResourceUtil.class.getClassLoader()
				.getResourceAsStream(resName);
		File file = new File(destFile);
		OutputStream out = new FileOutputStream(file);
		FileUtil.cpyStream(in, out);
	}

	public static void loadAndroidLibrary(String libName) {
		// 如果有shell权限，从lib读取so，释放到/data/local/tmp/,然后加载
		int uid = android.os.Process.myUid();
		if (uid != 2000 && uid != 0) {
			return;
		}
		File libDir = new File("/data/local/tmp/lib");
		if (!libDir.exists()) {
			libDir.mkdir();
		}
		for (String cpu : CPU_ABIS) {
			String srcName = "lib/" + cpu + "/lib" + libName + ".so";
			File outFile = new File(libDir, "lib" + libName + ".so");
			try {
				releaseFile(srcName, outFile.getPath());
			} catch (IOException e) {
			}
			outFile.setExecutable(true);
			outFile.setReadable(true);
			outFile.setWritable(true);
			try {
				System.load(outFile.getPath());
				System.out.println("load lib ok");
				break;
			} catch (Throwable e) {
			}
		}
	}
}
