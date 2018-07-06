package com.jsdroid.findpic;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.jsdroid.util.PicUtil.Pic;

public class FindPic {

	// 找色
	static native void nativeFindColor(int[] pic, int left, int top, int right,
			int bottom, int color, int offset, int[] output);

	// 找图
	static native void nativeFindPic(int[] picBig, int bigWidth, int bigHeight,
			int[] picSmall, int smallWidth, int smallHeight, int left, int top,
			int right, int bottom, int offset, float sim, int[] output);
	
	public static Point findPic(Bitmap big, Bitmap small, int left, int top,
			int right, int bottom, int offset, float sim) {
		Point ret = new Point();
		int[] picBig = bitmap2arr(big);
		int bigWidth = big.getWidth();
		int bigHeight = big.getHeight();
		int[] picSmall = bitmap2arr(small);
		int smallWidth = small.getWidth();
		int smallHeight = small.getHeight();
		int[] output = new int[2];
		try {
			nativeFindPic(picBig, bigWidth, bigHeight, picSmall, smallWidth,
					smallHeight, left, top, right, bottom, offset, sim, output);
		} catch (Throwable e) {
		}
		ret.x = output[0];
		ret.y = output[1];
		return ret;
	}

	public static Point findPic(Pic big, Pic small, int left, int top,
			int right, int bottom, int offset, float sim) {
		Point ret = new Point();
		int[] picBig = big.pixels;
		int bigWidth = big.width;
		int bigHeight = big.height;
		int[] picSmall = small.pixels;
		int smallWidth = small.width;
		int smallHeight = small.height;
		int[] output = new int[2];
		try {
			nativeFindPic(picBig, bigWidth, bigHeight, picSmall, smallWidth,
					smallHeight, left, top, right, bottom, offset, sim, output);
		} catch (Throwable e) {
		}
		ret.x = output[0];
		ret.y = output[1];
		return ret;
	}

	public static int[] bitmap2arr(Bitmap bmp) {
		// return PicUtil.bitmap2Pic(bmp).pixels;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int[] arr = new int[width * height];
		bmp.getPixels(arr, 0, width, 0, 0, width, height);
		return arr;
	}
}
