package com.jsdroid.util;

/**
 * 
 * @author who
 * 
 */
public class PicUtil {
	public static class Point {
		public int x;
		public int y;
		public int color;

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + ", color="
					+ Integer.toHexString(color) + "]";
		}

	}

	public static class Pic {
		public int width;
		public int height;
		public int[] pixels;

		@Override
		public String toString() {
			return "Pic [width=" + width + ", height=" + height + "]";
		}

		public long getMemorySize() {
			return width * height * 4;
		}
	}

	public static int r(int color) {
		return (color >> 16) & 0xff;
	}

	public static int g(int color) {
		return (color >> 8) & 0xff;
	}

	public static int b(int color) {
		return color & 0xff;
	}

	/**
	 * 得到亮度
	 * 
	 * @param color
	 * @return
	 */
	public static int light(int color) {
		return r(color) + g(color) + b(color);
	}

	/**
	 * 得到灰度
	 * 
	 * @param color
	 * @return
	 */
	public static int gray(int color) {
		int light = light(color) / 3;
		return light;
	}

	/**
	 * 由灰度变成颜色
	 * 
	 * @param gray
	 * @return
	 */
	public static int grayColor(int gray) {
		return rgb(gray, gray, gray);
	}

	public static int rgb(int red, int green, int blue) {
		return (0xFF << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * 
	 * @param pic
	 * @return 返回最高和最低亮度点
	 */
	public static Point[] minMaxLightPoint(Pic pic) {
		Point[] minMaxLightPoint = new Point[2];
		minMaxLightPoint[0] = new Point();
		minMaxLightPoint[1] = new Point();

		int minLight = -1;
		int maxLight = -1;
		for (int j = 0; j < pic.height; j++) {
			int off = j * pic.width;
			for (int i = 0; i < pic.width; i++) {
				int pos = off + i;
				int color = pic.pixels[pos];
				// �����ɫΪ0����ô����
				if (color == 0) {
					continue;
				}
				int light = light(color);
				if (minLight == -1) {
					maxLight = minLight = light;
					minMaxLightPoint[0].x = i;
					minMaxLightPoint[0].y = j;
					minMaxLightPoint[0].color = color;
					minMaxLightPoint[1].x = i;
					minMaxLightPoint[1].y = j;
					minMaxLightPoint[1].color = color;
				} else {
					if (light < minLight) {
						minMaxLightPoint[0].x = i;
						minMaxLightPoint[0].y = j;
						minMaxLightPoint[0].color = color;
					}
					if (light > maxLight) {
						minMaxLightPoint[1].x = i;
						minMaxLightPoint[1].y = j;
						minMaxLightPoint[1].color = color;
					}
				}

			}
		}
		return minMaxLightPoint;
	}

	/**
	 * 比较颜色
	 * 
	 * @param color1
	 * @param color2
	 * @param offr
	 * @param offg
	 * @param offb
	 * @return 如果颜色类似返回true,否则返回false
	 */
	public static boolean compareColor(int color1, int color2, int offr,
			int offg, int offb) {
		int r1 = (color1 >> 16) & 0xff;
		int r2 = (color2 >> 16) & 0xff;
		if (Math.abs(r1 - r2) > offr) {
			return false;
		}
		int g1 = (color1 >> 8) & 0xff;
		int g2 = (color2 >> 8) & 0xff;
		if (Math.abs(g1 - g2) > offg) {
			return false;
		}
		int b1 = color1 & 0xff;
		int b2 = color2 & 0xff;
		if (Math.abs(b1 - b2) > offb) {
			return false;
		}
		return true;
	}

	/**
	 * 查找颜色
	 * 
	 * @param pic
	 * @param color
	 * @param offset
	 * @return
	 */
	public static Point findColor(Pic pic, int left, int top, int right,
			int bottom, int color, int offset) {
		if (left < 0) {
			left = 0;
		}
		if (top < 0) {
			top = 0;
		}
		if (right > pic.width) {
			right = pic.width;
		}
		if (bottom > pic.height) {
			bottom = pic.height;
		}
		if (0 == left && left == top && top == right && right == bottom) {
			right = pic.width;
			bottom = pic.height;
		}
		int offr = r(offset);
		int offg = g(offset);
		int offb = b(offset);
		int off = 0;
		for (int j = top; j < bottom; j++) {
			for (int i = left; i < right; i++) {
				int pos = off + i;
				if (compareColor(color, pic.pixels[pos], offr, offg, offb)) {
					Point p = new Point();
					p.x = i;
					p.y = j;
					p.color = pic.pixels[pos];
					return p;
				}
			}
			off += pic.width;
		}
		return null;
	}

	/**
	 * 
	 * @param bigPic
	 * @param smallPic
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param offset
	 * @param sim
	 * @return
	 */
	public static Point findPic(Pic bigPic, Pic smallPic, int left, int top,
			int right, int bottom, int offset, float sim) {
		if (left < 0) {
			left = 0;
		}
		if (top < 0) {
			top = 0;
		}
		if (right > bigPic.width) {
			right = bigPic.width;
		}
		if (bottom > bigPic.height) {
			bottom = bigPic.height;
		}
		if (0 == left && left == top && top == right && right == bottom) {
			right = bigPic.width;
			bottom = bigPic.height;
		}
		int offr = r(offset);
		int offg = g(offset);
		int offb = b(offset);

		Point[] minMaxLightPoint = minMaxLightPoint(smallPic);

		Point minLightPoint = minMaxLightPoint[0];
		Point maxLightPoint = minMaxLightPoint[1];
		int bigPicOff = top * bigPic.width;
		for (int bigY = top; bigY < bottom - smallPic.height; bigY++) {
			for (int bigX = left; bigX < right - smallPic.width; bigX++) {
				{

					int minLightColorPos = bigPicOff + minLightPoint.y
							* bigPic.width + bigX + minLightPoint.x;

					int bigPicColor = bigPic.pixels[minLightColorPos];

					if (compareColor(bigPicColor, minLightPoint.color, offr,
							offg, offb) == false) {
						continue;
					}
				}
				{
					int maxLightColorPos = bigPicOff + maxLightPoint.y
							* bigPic.width + bigX + maxLightPoint.x;

					int bigPicColor = bigPic.pixels[maxLightColorPos];
					if (compareColor(bigPicColor, maxLightPoint.color, offr,
							offg, offb) == false) {
						continue;
					}
				}
				{
					int maxDisCount = (int) ((1 - sim) * smallPic.width * smallPic.height);
					int maxSucCount = (int) (sim * smallPic.width * smallPic.height);
					int smallPicOff = 0;
					int disCount = 0;
					int sucCount = 0;
					int bigPicOffOff = bigPicOff;

					FindPic: for (int smallY = 0; smallY < smallPic.height; smallY++) {
						for (int smallX = 0; smallX < smallPic.width; smallX++) {
							int bigPicColor = bigPic.pixels[bigPicOffOff + bigX
									+ smallX];
							int smallPicColor = smallPic.pixels[smallPicOff
									+ smallX];
							if (compareColor(bigPicColor, smallPicColor, offr,
									offg, offb)) {
								sucCount++;
								if (sucCount > maxSucCount) {
									break FindPic;
								}
							} else {
								disCount++;
								if (disCount > maxDisCount) {
									break FindPic;
								}
							}

						}
						smallPicOff += smallPic.width;
						bigPicOffOff += bigPic.width;
					}
					if (sucCount > maxSucCount || disCount <= maxDisCount) {
						Point p = new Point();
						p.x = bigX;
						p.y = bigY;
						return p;
					}
				}
			}
			bigPicOff += bigPic.width;
		}
		return null;
	}
}
