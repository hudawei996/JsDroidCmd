package com.jsdroid.record;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;

import com.jsdroid.util.BitmapUtil;

public class SaveImageThread extends Thread {
	static Paint paint = new Paint();
	Point point;
	String imgFile;
	Bitmap bmp;

	public SaveImageThread(Point point, String imgFile, Bitmap bmp) {
		super();
		this.point = point;
		this.imgFile = imgFile;
		this.bmp = bmp;
	}

	public SaveImageThread(String imgFile, Bitmap bmp) {
		super();
		this.imgFile = imgFile;
		this.bmp = bmp;
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		BitmapUtil.save(imgFile, bmp);
		bmp.recycle();
		long end = System.currentTimeMillis();
		System.out.println("save use time:" + (end - start));
	}
}
