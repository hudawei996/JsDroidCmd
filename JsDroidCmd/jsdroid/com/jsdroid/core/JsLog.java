package com.jsdroid.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import com.jsdroid.util.FileUtil;

public class JsLog {
	static final String LOG_FILE = "/data/local/tmp/js_log";

	public static void log(Object... obj) {
		if (obj != null) {
			for (int i = 0; i < obj.length; i++) {
				if (obj[i] != null) {
					System.out.println(obj[i] + "");
					FileUtil.append(LOG_FILE, obj[i].toString());
				}
			}
		}
	}

	public static void err(Throwable... err) {
		if (err != null) {
			for (int i = 0; i < err.length; i++) {
				Throwable e = err[i];
				if (e != null) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(out);
					e.printStackTrace(pw);
					String str = out.toString();
					FileUtil.append(LOG_FILE, str);
				}
			}
		}
	}
}
