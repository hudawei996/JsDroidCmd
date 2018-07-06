package com.jsdroid.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mozilla.javascript.Context;

import android.os.ServiceManager;

import com.google.gson.Gson;
import com.jsdroid.http.NanoHTTPD;
import com.jsdroid.http.NanoHTTPD.Response;
import com.jsdroid.util.SocketUtil;

public class JsHttpServer extends Thread {
	public static final int PORT = 9800;
	private static ExecutorService executorService = Executors
			.newCachedThreadPool();

	public static synchronized void execute(Runnable runnable) {
		executorService.execute(runnable);
	}

	public static synchronized void shutdown() {
		executorService.shutdownNow();
		executorService = Executors.newCachedThreadPool();
	}

	public JsHttpServer() {
		start();
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(PORT);
			for (;;) {
				Socket socket = server.accept();
				SocketUtil socketUtil = new SocketUtil(socket);
				execute(new Client(socketUtil));
			}
		} catch (IOException e) {
		}

	}

	class Client extends Thread {
		private static final long TIMEOUT = 10000;
		SocketUtil socketUtil;
		Gson gson = new Gson();

		public Client(SocketUtil socketUtil) {
			this.socketUtil = socketUtil;
		}

		@Override
		public void run() {
			try {
				String code = null;
				for (;;) {
					String line = socketUtil.readLine();
					if (line == null) {
						break;
					}

					if (line.startsWith("POST") || line.startsWith("GET")) {
						String texts[] = line.split(" ");
						code = "";
						for (int i = 1; i < texts.length - 1; i++) {
							code += " " + texts[i];
						}
						code = code.trim();
						if (code.startsWith("/?js=")) {
							code = code.substring("/?js=".length());
							code = URLDecoder.decode(code);
							break;
						} else if (code.startsWith("/?close")) {
							System.out.println("http close");
							System.exit(0);
						} else if (code.startsWith("/?pause")) {
							shutdown();
						}
					}

				}
				JsUserCase jsUseCase = new JsUserCase();
				jsUseCase.type = JsUserCase.TYPE_TEXT;
				jsUseCase.source = code;
				JsThread thread = new JsThread(jsUseCase, JsGlobal.getGlobal());
				thread.start();
				JsResult result = thread.waitResult(TIMEOUT);
				String ret = "超过等待时间,后台线程运行中(使用pause可以停止所有线程)...";
				if (result != null) {
					ret = Context.toString(result.result);
				}
				Response response = NanoHTTPD.newFixedLengthResponse(ret);
				response.send(socketUtil.getSocket().getOutputStream());
			} catch (Exception e) {
			} finally {
				try {
					socketUtil.close();
				} catch (IOException e) {
				}
			}

		}
	}
}
