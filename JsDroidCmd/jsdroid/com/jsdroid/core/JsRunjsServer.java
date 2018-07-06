package com.jsdroid.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mozilla.javascript.Context;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.google.gson.Gson;
import com.jsdroid.util.FileUtil;
import com.jsdroid.util.LocalSocketUtil;

public class JsRunjsServer extends Thread {
	private static ExecutorService executorService = Executors
			.newCachedThreadPool();

	public static synchronized void execute(Runnable runnable) {
		executorService.execute(runnable);
	}

	public static synchronized void shutdown() {
		executorService.shutdownNow();
		executorService = Executors.newCachedThreadPool();
	}

	public JsRunjsServer() {
		start();
	}

	@Override
	public void run() {
		LocalServerSocket server;
		try {
			server = new LocalServerSocket("jsdroid");
			for (;;) {
				LocalSocket socket = server.accept();
				execute(new Client(socket));
			}
		} catch (Exception e) {
			try {
				e.printStackTrace(new PrintStream("/data/local/tmp/err"));
			} catch (FileNotFoundException er) {
			}
		}

	}

	class Client extends Thread {
		LocalSocket socket;
		Gson gson = new Gson();

		public Client(LocalSocket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				LocalSocketUtil socketUtil = new LocalSocketUtil(socket);
				String file = socketUtil.readLine();
				String code = FileUtil.read(file);
				JsUserCase jsUseCase = new JsUserCase();
				jsUseCase.type = JsUserCase.TYPE_TEXT;
				jsUseCase.source = code;
				JsThread thread = new JsThread(jsUseCase, JsGlobal.getGlobal());
				thread.start();
				JsResult result = thread.waitResult();
				if (result != null) {
					result.result = Context.toString(result.result);
				} else {
					result.result = "";
				}
				socketUtil.sendLine("" + result.result);
			} catch (Exception e) {
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}

		}
	}
}
