package com.jsdroid.core;

import java.io.IOException;
import java.util.UUID;

import android.graphics.Bitmap;
import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.google.gson.Gson;
import com.jsdroid.record.JsRecord;
import com.jsdroid.record.Recorder;
import com.jsdroid.record.Recorder.RecordListener;
import com.jsdroid.uiautomator2.UiDevice;
import com.jsdroid.util.BitmapUtil;
import com.jsdroid.util.LocalSocketUtil;

public class JsLocalServer extends Thread {
	String serverName = "jsdroid_cmd";
	private Object lock;
	private boolean start;

	public JsLocalServer() {
		// 尝试连接
		try {
			new LocalSocketUtil(serverName).close();
			// 连接成功，退出
			System.exit(0);
		} catch (IOException e1) {
		}
		lock = new Object();
		start = false;
		start();
		synchronized (lock) {
			if (start == false) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public boolean isStart() {
		return start;
	}

	@Override
	public void run() {
		LocalServerSocket server;
		try {
			server = new LocalServerSocket(serverName);
			JsSystem.deviceInfo.canUseLocalSocket = true;
			synchronized (lock) {
				start = true;
				lock.notifyAll();
			}
			for (;;) {
				LocalSocket socket = server.accept();
				LocalSocketUtil socketUtil = new LocalSocketUtil(socket);
				new Client(socketUtil).start();
			}
		} catch (Exception e) {
		} finally {
			synchronized (lock) {
				lock.notifyAll();
			}
		}

	}

	class Client extends Thread implements JsEventListner {
		LocalSocketUtil socketUtil;
		Gson gson = new Gson();
		JsThread jsThread;

		void heart() {
			// 心跳
			new Thread() {
				public void run() {
					while (true) {
						if (Client.this.socketUtil != null) {
							JsCmd jsCmd = new JsCmd();
							jsCmd.type = -1;
							try {
								Client.this.socketUtil.sendLine(gson
										.toJson(jsCmd));
							} catch (IOException e) {
								try {
									Client.this.socketUtil.close();
								} catch (IOException e1) {
								}
								break;
							}
						}
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
						}
					}
				};
			}.start();
		}

		public Client(LocalSocketUtil socketUtil) {
			this.socketUtil = socketUtil;
			// 心跳
			// heart
		}

		@Override
		public void run() {
			JsEvent.getInstance().addEventListener(this);
			try {
				for (;;) {
					String json = socketUtil.readLine();
					if (json == null) {
						// 已关闭连接
						break;
					}
					JsCmd cmd = gson.fromJson(json, JsCmd.class);
					switch (cmd.type) {
					case JsCmd.TYPE_RUN: {
						runScript(cmd);
						break;
					}
					case JsCmd.TYPE_STOP_SCRIPT: {
						try {
							stopScript();
						} catch (Exception e) {
						}
						break;
					}
					case JsCmd.TYPE_CAPTURE: {
						capture(cmd);
						break;
					}
					case JsCmd.TYPE_RECORD: {
						record();
						break;
					}
					case JsCmd.TYPE_STOP_RECORD: {
						Recorder.getInstance().quit();
						break;
					}
					case JsCmd.TYPE_CLOSE:
						System.exit(0);
						break;
					}
				}
			} catch (Exception e) {
			} finally {
				try {
					JsEvent.getInstance().removeListener(this);
				} catch (Exception e) {
				}
				try {
					Recorder.getInstance().quit();
				} catch (Exception e) {
				}
				if (jsThread != null) {
					jsThread.quit();
				}
				try {
					socketUtil.close();
				} catch (Exception e) {
				}
			}

		}

		private void sendClose() {
			JsCmd cmd = new JsCmd();
			cmd.type = JsCmd.TYPE_CLOSE;
			try {
				socketUtil.sendLine(gson.toJson(cmd));
			} catch (Exception e) {
			}
			try {
				socketUtil.close();
			} catch (Exception e) {
			}
		}

		private void stopScript() {
			if (jsThread != null) {
				try {
					jsThread.quit();
				} catch (Exception e) {
				}
			}
		}

		private void runScript(JsCmd cmd) {
			if (jsThread != null) {
				jsThread.quit();
			}
			JsUserCase usercase = gson.fromJson(cmd.data, JsUserCase.class);
			JsGlobal.getGlobal().setJsAppInterface(new JsAppInterface() {

				@Override
				public void toast(String obj) {
					JsCmd cmd = new JsCmd();
					cmd.type = JsCmd.TYPE_TOAST;
					cmd.data = obj;
					try {
						socketUtil.sendLine(gson.toJson(cmd));
					} catch (IOException e) {
						try {
							socketUtil.close();
						} catch (IOException e1) {
						}
					}
				}

				@Override
				public void print(String obj) {
					JsCmd cmd = new JsCmd();
					cmd.type = JsCmd.TYPE_PRINT;
					cmd.data = obj;
					try {
						socketUtil.sendLine(gson.toJson(cmd));
					} catch (IOException e) {
						try {
							socketUtil.close();
						} catch (IOException e1) {
						}
					}
				}

				@Override
				public void log(String obj) {
					JsCmd cmd = new JsCmd();
					cmd.type = JsCmd.TYPE_LOG;
					cmd.data = obj;
					try {
						socketUtil.sendLine(gson.toJson(cmd));
					} catch (IOException e) {
						try {
							socketUtil.close();
						} catch (IOException e1) {
						}
					}
				}

				@Override
				public void input(String obj) {
					JsCmd cmd = new JsCmd();
					cmd.type = JsCmd.TYPE_INPUT;
					cmd.data = obj;
					try {
						socketUtil.sendLine(gson.toJson(cmd));
					} catch (IOException e) {
						try {
							socketUtil.close();
						} catch (IOException e1) {
						}
					}
				}
			});
			// 打开

			jsThread = new JsThread(usercase, JsGlobal.getGlobal());
			jsThread.setListener(new JsThread.Listener() {
				@Override
				public void onEnd(JsResult result) {

					JsCmd cmd = new JsCmd();
					cmd.type = JsCmd.TYPE_RESULT;
					cmd.data = gson.toJson(result);
					try {
						socketUtil.sendLine(gson.toJson(cmd));
					} catch (Exception e) {
						try {
							socketUtil.close();
						} catch (Exception e1) {
						}
					}
				}
			});
			jsThread.start();
		}

		private void record() {
			Recorder recorder = Recorder.getInstance();
			recorder.init();
			recorder.setRecordListener(new RecordListener() {

				@Override
				public void onRecord(JsRecord record) {
					JsCmd cmd = new JsCmd();
					cmd.type = JsCmd.TYPE_RECORD;
					cmd.data = gson.toJson(record);
					try {
						socketUtil.sendLine(gson.toJson(cmd));
					} catch (IOException e) {
					}
				}
			});
			recorder.start();
		}

		private void capture(JsCmd cmd) throws IOException {
			JsCapture capture = new JsCapture();
			capture.imageFile = "/sdcard/jsdroid/images/"
					+ UUID.randomUUID().toString() + ".png";
			// 截图
			capture.rotation = UiDevice.getInstance().getAutomatorBridge()
					.getRotation();
			capture.imageWidth = UiDevice.getInstance()
					.getDisplayUnRotationWidth();
			capture.imageHeight = UiDevice.getInstance()
					.getDisplayUnRotationHeight();
			float scale = 1;
			int screenWidth = UiDevice.getInstance()
					.getDisplayUnRotationWidth();
			int screenHeight = UiDevice.getInstance()
					.getDisplayUnRotationHeight();
			if (screenWidth < 480 || screenHeight < 480) {
				scale = 1;
			} else if (screenWidth < screenHeight) {
				scale = (float) (480.0 / screenWidth);
			} else {
				scale = (float) (480.0 / screenHeight);
			}
			capture.imageWidth = (int) (screenWidth * scale);
			capture.imageHeight = (int) (screenHeight * scale);

			try {
				Bitmap bmp = BitmapUtil
						.takeScreenshot(UiDevice.getInstance().getRotation(),
								capture.imageWidth, capture.imageHeight);
				BitmapUtil.save(capture.imageFile, bmp);
				bmp.recycle();
			} catch (Exception e) {
			}
			// 获取节点
			capture.nodes = UiDevice.getInstance().getNodes();
			// 获取act
			try {
				capture.act = UiDevice.getInstance().getAct();
			} catch (Exception e) {
			}
			// 发送结果
			cmd.type = JsCmd.TYPE_CAPTURE;
			cmd.data = gson.toJson(capture);
			socketUtil.sendLine(gson.toJson(cmd));
		}

		@Override
		public void onEvent(int[] event) {
			if (event[0] == JsEvent.EV_KEY && event[1] == 114 && event[2] == 0) {
				// 音量减
				new Thread() {
					public void run() {
						JsCmd cmd = new JsCmd();
						cmd.type = JsCmd.TYPE_VOLUME_DOWN;
						try {
							socketUtil.sendLine(gson.toJson(cmd));
						} catch (IOException e) {
						}
					};
				}.start();

			}
		}
	}
}
