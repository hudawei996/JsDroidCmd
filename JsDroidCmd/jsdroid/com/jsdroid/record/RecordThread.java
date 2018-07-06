package com.jsdroid.record;

import java.util.List;
import java.util.UUID;

import android.graphics.Bitmap;

import com.jsdroid.record.Recorder.RecordListener;
import com.jsdroid.uiautomator2.UiDevice;
import com.jsdroid.util.BitmapUtil;

import eu.chainfire.libsuperuser.Shell;

public class RecordThread extends Thread {
	JsRecord jsRecord;
	Bitmap screen;
	RecordListener recordListener;
	boolean end;

	public RecordThread(JsRecord jsRecord, RecordListener recordListener) {
		super();
		this.jsRecord = jsRecord;
		this.recordListener = recordListener;
	}

	@Override
	public void run() {
		// 获取屏幕方向
		getRotation();
		// 截图
		capture();
		// 获取节点
		getNode();
		// 保存图片
		saveCapture();
		while (end == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		// 发送
		if (recordListener != null) {
			recordListener.onRecord(jsRecord);
		}
	}

	// 发送结果
	public void endAndSend() {
		end = true;
	}

	public void endNoSend() {
		recordListener = null;
		end = true;
	}

	private void getRotation() {
		jsRecord.rotation = UiDevice.getInstance().getAutomatorBridge()
				.getRotation();
	}

	private void getNode() {
		jsRecord.nodes = UiDevice.getInstance().getNodes();
	}

	private void capture() {
		try {

			screen = BitmapUtil.takeScreenshot(jsRecord.rotation,
					jsRecord.imageWidth, jsRecord.imageHeight);
		} catch (Exception e) {
		}
	}

	private void saveCapture() {
		String picName = "/sdcard/jsdroid/images/"
				+ UUID.randomUUID().toString() + ".png";
		BitmapUtil.save(picName, screen);
		screen.recycle();
		jsRecord.imageName = picName;
	}
}
