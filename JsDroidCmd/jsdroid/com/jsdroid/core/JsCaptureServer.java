package com.jsdroid.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.google.gson.Gson;
import com.jsdroid.bean.CaptureInfo;
import com.jsdroid.bean.CaptureOption;
import com.jsdroid.uiautomator2.UiDevice;
import com.jsdroid.util.BitmapUtil;
import com.jsdroid.util.SocketUtil;

public class JsCaptureServer extends Thread {
	public static final int PORT = 9804;

	public JsCaptureServer() {
		start();
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(PORT);
			for (;;) {
				Socket socket = server.accept();
				new Client(socket).start();
			}
		} catch (IOException e) {
		}

	}

	class Client extends Thread {
		Gson gson = new Gson();
		JsThread jsThread;
		Socket socket;
		ZipOutputStream zipOut;
		CaptureOption captureOption;

		public Client(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				String option = new SocketUtil(socket).readLine();
				captureOption = gson.fromJson(option, CaptureOption.class);
			} catch (IOException e) {
			}
			try {
				if (captureOption == null) {
					throw new Exception("option error!");
				}
				capture();
			} catch (Exception e) {
			} finally {
				try {
					zipOut.close();
				} catch (Exception e) {
				}
				try {
					socket.close();
				} catch (Exception e) {
				}

			}
		}

		private void capture() throws IOException {

			CaptureInfo captureInfo = new CaptureInfo();
			// 截图
			int rotation = UiDevice.getInstance().getAutomatorBridge()
					.getRotation();

			int screenWidth = UiDevice.getInstance().getDisplayWidth();
			int screenHeight = UiDevice.getInstance().getDisplayHeight();

			int width = (int) (captureOption.scale * screenWidth + 0.5f);
			int height = (int) (captureOption.scale * screenHeight + 0.5f);
			if (width == 0 || height == 0) {
				width = screenWidth;
				height = screenHeight;
			}
			captureInfo.rotation = rotation;
			captureInfo.screenWidth = screenWidth;
			captureInfo.screenHeight = screenHeight;
			captureInfo.imageWidth = width;
			captureInfo.imageHeight = height;

			Bitmap image = null;
			try {
				image = BitmapUtil.takeScreenshot(rotation, width, height);
			} catch (Exception e) {
			}

			// 获取节点
			captureInfo.nodes = UiDevice.getInstance().getNodes();
			// 获取act
			try {
				captureInfo.act = UiDevice.getInstance().getAct();
			} catch (Exception e) {
			}

			ByteArrayOutputStream zipOutByteStream = new ByteArrayOutputStream();
			// 发送结果
			zipOut = new ZipOutputStream(zipOutByteStream);
			String infoJson = gson.toJson(captureInfo);
			byte[] infoBytes = infoJson.getBytes();
			// 发送info
			ZipEntry entry = new ZipEntry("info");
			entry.setSize(infoBytes.length);
			zipOut.putNextEntry(entry);
			zipOut.write(infoBytes);
			if (image != null) {
				ByteArrayOutputStream bmpOut = new ByteArrayOutputStream();
				image.compress(CompressFormat.PNG, captureOption.quality,
						bmpOut);
				ZipEntry imageEntry = new ZipEntry("image");
				imageEntry.setSize(bmpOut.size());
				zipOut.putNextEntry(imageEntry);
				zipOut.write(bmpOut.toByteArray());
				image.recycle();
			}
			zipOut.close();
			socket.getOutputStream().write(
					IntToByteArray(zipOutByteStream.size()));
			socket.getOutputStream().write(zipOutByteStream.toByteArray());
		}
	}

	public byte[] IntToByteArray(int n) {
		byte[] b = new byte[4];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}

	public int ByteArrayToInt(byte[] bArr) {
		if (bArr.length != 4) {
			return -1;
		}
		return (int) ((((bArr[3] & 0xff) << 24) | ((bArr[2] & 0xff) << 16)
				| ((bArr[1] & 0xff) << 8) | ((bArr[0] & 0xff) << 0)));
	}
}
