package com.jsdroid.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class Ocr {
	public static String ocr(int left, int top, int right, int bottom)
			throws IOException {
		Bitmap bmp = BitmapUtil.takeScreenshot();
		Bitmap image = Bitmap.createBitmap(bmp, left, top, right - left, bottom
				- top);
		String result = ocr(image);
		image.recycle();
		bmp.recycle();
		return result;
	}

	public static String ocr(Bitmap image) throws IOException {
		// ============ 读图片数据===========
		// DataInputStream in = new DataInputStream(new FileInputStream(img));
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(CompressFormat.JPEG, 100, stream);
		stream.close();
		return ocr(stream.toByteArray());
	}

	public static String ocr(byte[] image, int i) throws IOException {

		URL api = new URL("http://ocr.shouji.sogou.com/v2/ocr/json");

		String boundary = "WebKitFormBoundaryigB5AAPpGvuxQDH";
		HttpURLConnection conn = HttpURLConnection.class.cast(api
				.openConnection());

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=----" + boundary);

		PrintStream out = new PrintStream(conn.getOutputStream());
		// OutputStream out = new DataOutputStream(conn.getOutputStream());

		StringBuffer sb = new StringBuffer();

		// ////////////////////// 上传数据////////////////////////
		sb.append("\r\n------" + boundary + "\r\n");
		sb.append("Content-Disposition: form-data; name=\"pic\"; filename=\"/sdcard/pic.jpg\"\r\n");
		sb.append("Content-Type:image/jpeg\r\n\r\n");

		out.write(sb.toString().getBytes());

		// ============ 读图片数据===========
		// DataInputStream in = new DataInputStream(new FileInputStream(img));
		out.write(image, 0, image.length);
		String end = "\r\n------" + boundary + "--\r\n";
		out.write(end.getBytes());
		out.flush();
		out.close();

		// ///////////////////// 上传数据end////////////////////////

		BufferedReader read = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "utf-8"));
		StringBuffer json = new StringBuffer();
		String line = null;
		while ((line = read.readLine()) != null) {
			json.append(line);
		}

		read.close();
		return json.toString();
	}

	public static void main(String[] args) throws IOException {
		byte[] image = FileUtil.readBytes("test.jpg");
		System.out.println(getOcrText(ocr(image)));
	}

	public static String getOcrText(String ocr) {
		StringBuilder result = new StringBuilder();
		try {
			JsonElement ele = new JsonParser().parse(ocr);
			if (ele.isJsonObject()) {
				JsonObject jobj = ele.getAsJsonObject();

				ele = jobj.get("result");
				if (ele.isJsonArray()) {
					JsonArray arr = ele.getAsJsonArray();
					for (int i = 0; i < arr.size(); i++) {
						String text = arr.get(i).getAsJsonObject()
								.get("content").getAsString();
						result.append(text + "\n");
					}
				}
			}
		} catch (JsonSyntaxException e) {
			return null;
		}
		return result.toString();
	}

	public static String ocr(byte[] imageData) throws IOException {

		URL api = new URL("http://ocr.shouji.sogou.com/v2/ocr/json");

		String boundary = "WebKitFormBoundaryigB5AAPpGvluxQDH";
		HttpURLConnection conn = (HttpURLConnection) api.openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);

		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=----" + boundary);

		PrintStream out = new PrintStream(conn.getOutputStream());
		StringBuffer sb = new StringBuffer();

		// ////////////////////// 上传数据////////////////////////
		sb.append("\r\n------" + boundary + "\r\n");
		sb.append("Content-Disposition: form-data; name=\"pic\"; filename=\"pic.jpg\"\r\n");
		sb.append("Content-Type:image/jpeg\r\n\r\n");

		out.write(sb.toString().getBytes());

		// ============ 读图片数据===========
		// DataInputStream in = new DataInputStream(new FileInputStream(img));
		// FileInputStream in = new FileInputStream(img);
		//
		// byte[] data = new byte[in.available()];
		// in.read(data);
		out.write(imageData, 0, imageData.length);

		String end = "\r\n------" + boundary + "--\r\n";
		out.write(end.getBytes());

		out.flush();
		out.close();

		// ///////////////////// 上传数据end////////////////////////

		BufferedReader read = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "utf-8"));
		StringBuffer json = new StringBuffer();
		String line = null;
		while ((line = read.readLine()) != null) {
			json.append(line);
		}

		read.close();
		return json.toString();
	}

}
