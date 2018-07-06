package com.jsdroid.findimg;

import android.graphics.Bitmap;

public class FindImg {
	public static class Img {
		public int width;
		public int height;
		public int[] pixels;
	}

	public static class Rect {
		public int left = -1;
		public int top = -1;
		public int right = -1;
		public int bottom = -1;

		@Override
		public String toString() {
			return "Rect{" + "left=" + left + ", top=" + top + ", right="
					+ right + ", bottom=" + bottom + '}';
		}
	}

	static int output[] = new int[4];

	public synchronized static Rect findImg(Img big, Img small, int level,
			int left, int top, int right, int bottom, int offset, int distance,
			float sim) {

		nativeFindImg(big.pixels, big.width, big.height, small.pixels,
				small.width, small.height, level, left, top, right, bottom,
				offset, distance, sim, output);
		Rect rect = new Rect();
		rect.left = output[0];
		rect.top = output[1];
		rect.right = output[2];
		rect.bottom = output[3];
		return rect;

	}

	public synchronized static Img bitmap2Img(Bitmap bmp) {
		Img img = new Img();
		img.width = bmp.getWidth();
		img.height = bmp.getHeight();
		img.pixels = new int[img.width * img.height];
		bmp.getPixels(img.pixels, 0, img.width, 0, 0, img.width, img.height);
		return img;
	}

	public synchronized static Rect findImg(Bitmap big, Bitmap small,
			int level, int left, int top, int right, int bottom, int offset,
			int distance, float sim) {
		Img bg = bitmap2Img(big);
		Img sm = bitmap2Img(small);
		return findImg(bg, sm, level, left, top, right, bottom, offset,
				distance, sim);

	}

	static native void nativeFindImg(int[] imgBig, int bigWidth, int bigHeight,
			int[] imgSmall, int smallWidth, int smallHeight, int level,
			int left, int top, int right, int bottom, int offset, int distance,
			float sim, int[] output);

}
