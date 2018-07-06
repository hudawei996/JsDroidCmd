package com.jsdroid.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class JsMain {
	static Handler handler;
	static HandlerThread handlerThread;


	static void init() {
		// 全局异常处理
		JsExceptionListener.init();
		// 启动后台服务
		JsSystem.main();
	}


	public static void main(String[] args) {
		Looper.prepare();	
		init();
		Looper.loop();
	}
}
