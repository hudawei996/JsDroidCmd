package com.jsdroid.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.UnknownHostException;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

public class LocalSocketUtil {
	LocalSocket socket;

	public LocalSocket getSocket() {
		return socket;
	}

	BufferedReader reader;

	public LocalSocketUtil(LocalSocket socket) {
		this.socket = socket;
	}

	public LocalSocketUtil(String name) throws UnknownHostException,
			IOException {
		socket = new LocalSocket();
		socket.connect(new LocalSocketAddress(name));
	}

	public void setTimout(int timeout) {
		try {
			socket.setSoTimeout(timeout);
		} catch (IOException e) {
		}
	}

	public synchronized void sendLine(String line) throws IOException {
		OutputStream out = socket.getOutputStream();
		out.write((line + "\n").getBytes());
		out.flush();

	}

	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
		if (socket != null) {
			socket.close();
		}
	}

	public String readLine() throws IOException {
		if (reader == null) {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		}
		return reader.readLine();
	}

	public String readLine(String charset) throws IOException {
		if (reader == null) {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), charset));
		}
		return reader.readLine();
	}

	public static void main(String[] args) {
	}
}
