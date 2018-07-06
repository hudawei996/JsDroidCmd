/**
 * 一个运行js的类，单例模式，全局只有一个JavaScript脚本运行
 */
package com.jsdroid.core;

import java.io.File;

import org.mozilla.javascript.Context;

public class JsThread extends Thread {
	private JsUserCase mJsUseCase;
	private Context mContext;
	private JsGlobal mJsGlobal;
	private JsResult mJsResult;
	Thread mThread;

	public interface Listener {
		public void onEnd(JsResult result);
	}

	private Listener mListener;

	public JsThread(JsUserCase jsUseCase, JsGlobal global) {
		this.mJsUseCase = jsUseCase;
		this.mJsGlobal = global;
	}

	@Override
	public void run() {
		enter();
		mThread = Thread.currentThread();
		try {
			mJsResult = dowork();
		} catch (Throwable e) {
		} finally {
			if (mListener != null) {
				mListener.onEnd(mJsResult);
			}
		}

	}

	private JsResult dowork() {
		String scriptDir = null;
		mJsGlobal.pkg = mJsUseCase.pkg;
		if (mJsUseCase.filename != null) {
			scriptDir = new File(mJsUseCase.filename).getParent();
			mJsGlobal.scriptDir = scriptDir;
			mJsGlobal.put("scriptDir", mJsGlobal, scriptDir);
		} else if (mJsUseCase.directory != null) {
			scriptDir = new File(mJsUseCase.directory).getPath();
			mJsGlobal.put("scriptDir", mJsGlobal, scriptDir);
			mJsGlobal.scriptDir = scriptDir;
		}
		// mJsGlobal.put("config", mJsGlobal, mJsUseCase.options);
		mContext = Context.enter();
		mContext.setOptimizationLevel(-1);
		mContext.setDebugger(JsDebug.getInstance(), null);
		String source = null;
		String sourceName = "<script>";

		switch (mJsUseCase.type) {
		case JsUserCase.TYPE_TEXT:
			source = mJsUseCase.source;
			sourceName = "<script>";
			break;
		case JsUserCase.TYPE_FILE:
			source = JsInclude.readSource(mJsUseCase.filename, scriptDir);
			sourceName = mJsUseCase.filename;
			break;
		case 2:
			mJsUseCase.filename = mJsUseCase.directory + File.separator
					+ "main.js";
			source = JsInclude.readSource(mJsUseCase.filename, scriptDir);
			sourceName = mJsUseCase.filename;
			break;
		}
		JsResult jsResult;
		jsResult = new JsResult();
		try {
			if (source == null) {
				// 加载main.apk
				source = "loadapk('main.apk');include('main.js');";
				sourceName = "<loadapk>";
			}
			jsResult.result = mContext.evaluateString(mJsGlobal, source,
					sourceName, 1, null);
			jsResult.result = Context.toString(jsResult.result);
			jsResult.type = JsResult.RESULT_OK;
		} catch (Throwable e) {
			if (e instanceof JsStopException) {
				jsResult.type = JsResult.RESULT_OK;
				jsResult.result = "停止";
			} else {
				jsResult.type = JsResult.RESULT_ERR;
				jsResult.result = e.getMessage();
			}

		} finally {
			Context.exit();
		}
		return jsResult;
	}

	public JsResult waitResult() throws InterruptedException {
		return waitResult(0);
	}

	public JsResult waitResult(long timeout) throws InterruptedException {
		long endtime = System.currentTimeMillis() + timeout;
		while (true) {
			if (mJsResult == null || mJsResult.type == 0) {
				if (timeout != 0) {
					if (System.currentTimeMillis() > endtime) {

						return mJsResult;
					}
				}
				Thread.sleep(10);
			} else {
				return mJsResult;
			}
		}
	}

	public void enter() {
		mJsGlobal.enter();
	}

	public void quit() {
		try {
			mJsGlobal.quit();
			if (mThread != null) {
				mThread.interrupt();
			}
		} catch (Exception e) {
		}
	}

	public Listener getListener() {
		return mListener;
	}

	public void setListener(Listener listener) {
		this.mListener = listener;
	}
}
