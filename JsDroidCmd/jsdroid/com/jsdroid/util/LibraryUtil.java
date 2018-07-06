package com.jsdroid.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.jsdroid.core.JsLog;
import com.jsdroid.core.JsSystem;

public class LibraryUtil {
	static String abis[] = new String[] { "armeabi", "armeabi-v7a", "x86" };
	static String libs[] = new String[] { "event", "findimg", "findpic" };

	public static boolean loadAllLibrary(String outLibDir) {
		// 遍历文件夹，进行加载
		boolean loadok = false;
		for (int i = 0; i < abis.length; i++) {
			String abi = abis[i];
			for (int j = 0; j < libs.length; j++) {
				String name = libs[j];
				try {
					System.loadLibrary(name);
					loadok = true;
					continue;
				} catch (Throwable e) {
				}
				File outDir = new File(outLibDir, abi);
				outDir.mkdir();
				File outFile = new File(outDir, "lib" + name + ".so");

				InputStream input = ClassLoader
						.getSystemResourceAsStream("lib/" + abi + "/lib" + name
								+ ".so");
				try {
					FileUtil.cpyStream(input, new FileOutputStream(outFile));
				} catch (Exception e) {
					JsLog.err(e);
				}
				// 修改文件权限
				outFile.setExecutable(true);
				outFile.setWritable(true);
				outFile.setReadable(true);
				// 加载
				try {
					System.load(outFile.getPath());
					JsSystem.deviceInfo.cpu_load = abi;
					loadok = true;
				} catch (Throwable e) {
					break;
				}

			}
			if (loadok) {
				break;
			}
		}
		return loadok;
	}

	public static boolean loadAppLibrary(String pkg, String libName) {
		try {
			System.loadLibrary(libName);
			return true;
		} catch (Throwable e) {
		}
		String libFileName = System.mapLibraryName(libName);
		File appDir = null;
		appDir = new File("/data/app/" + pkg + "-1");
		if (!appDir.exists()) {
			appDir = new File("/data/app/" + pkg + "-2");
		}
		if (appDir.exists()) {
			if (appDir.isDirectory()) {
				if (appDir.getName().contains(pkg)) {
					File libDir = new File(appDir, "lib");
					for (File libFile : libDir.listFiles()) {
						if (libFile.isFile()) {
							if (libFile.getName().contains(libFileName)) {
								try {
									System.load(libFile.getPath());
									return true;
								} catch (Throwable e) {
								}
							}
						} else {
							for (File libFile1 : libFile.listFiles()) {
								if (libFile1.isFile()) {
									if (libFile1.getName()
											.contains(libFileName)) {
										try {
											System.load(libFile1.getPath());
											return true;
										} catch (Throwable e) {
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
}
