package com.jsdroid.core;

import java.lang.Thread.UncaughtExceptionHandler;

public class JsExceptionListener implements UncaughtExceptionHandler {

	// 系统默认的UncaughtException处理类
	Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static JsExceptionListener INSTANCE = new JsExceptionListener();

	public static JsExceptionListener init() {
		return INSTANCE;
	}

	private JsExceptionListener() {
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		JsLog.err(e);
	}

}
