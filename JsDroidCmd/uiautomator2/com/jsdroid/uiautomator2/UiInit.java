package com.jsdroid.uiautomator2;

import android.app.ActivityThread;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class UiInit {
	public static boolean start;
	private static HandlerThread handlerThread;
	private static Handler handler;
	private static Handler uiHandler;

	public static void runOnUiThread(Runnable runnable) {
		uiHandler.post(runnable);
	}

	public static void main() throws InterruptedException {
		handlerThread = new HandlerThread("jsdroid");
		handlerThread.start();
		Looper looper = handlerThread.getLooper();
		handler = new Handler(looper);
		Action action = new Action() {
			@Override
			public void action() {
				uiHandler = new Handler();
				try {
					// 初始化ActivityThread
					ActivityThread.systemMain();
				} catch (Throwable e) {
				}

				try {
					// 配置空闲等待时间
					Configurator.getInstance().setWaitForIdleTimeout(100);
					// 配置空闲等待间隔
					Configurator.getInstance().setWaitForSelectorTimeout(10);
					// 连接uiautomation服务
					UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
					automationWrapper.connect();
					UiDevice device = UiDevice.getInstance();
					// 初始化UiDevice
					device.initialize(new ShellUiAutomatorBridge(
							automationWrapper.getUiAutomation()));
					start = true;
				} catch (Throwable e) {
				}
			}
		};
		handler.post(action);
		// 等待action 结束
		action.waitFor();
	}

	public static abstract class Action implements Runnable {
		boolean stop;

		@Override
		public void run() {
			try {
				action();
			} finally {
				stop = true;
			}
		}

		public abstract void action();

		public void waitFor() throws InterruptedException {
			while (!stop) {
				Thread.sleep(10);
			}
		}
	}

}
