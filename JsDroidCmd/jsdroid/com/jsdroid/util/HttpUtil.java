package com.jsdroid.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

public class HttpUtil {
	public static String get(Map<String, Object> map) {
		String url = (String) map.get("url");
		if (url == null) {
			return null;
		}
		Map<String, String> headers = (Map<String, String>) map.get("headers");
		Map<String, String> params = (Map<String, String>) map.get("params");
		return get(url, headers, params);
	}

	public static String post(Map<String, Object> map) {
		String url = (String) map.get("url");
		if (url == null) {
			return null;
		}
		Map<String, String> headers = (Map<String, String>) map.get("headers");
		Map<String, String> params = (Map<String, String>) map.get("params");
		return post(url, headers, params);
	}

	public static String get(String url) throws IOException {
		return get(url, null, null);
	}

	public static String get(String url, Map<String, String> params)
			throws IOException {
		return get(url, null, params);
	}

	public static String post(String url, Map<String, String> params)
			throws IOException {
		return post(url, null, params);
	}

	public static String post(String url, byte[] params) {
		try {
			URL host = new URL(url);
			HttpURLConnection connection = HttpURLConnection.class.cast(host
					.openConnection());
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Charsert", "UTF-8");
			DataOutputStream dos = new DataOutputStream(
					connection.getOutputStream());
			if (params != null) {
				dos.write(params);
			}
			dos.flush();
			dos.close();

			int resultCode = connection.getResponseCode();
			StringBuilder response = new StringBuilder();
			if (resultCode == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					response.append(line);
				}
				br.close();
			} else {
				response.append(resultCode);
			}
			return response.toString();
		} catch (Throwable e) {
		}
		return null;
	}

	public static String post(String url, Map<String, String> headers,
			Map<String, String> params) {
		try {
			Connection connect = Jsoup.connect(url);
			connect.ignoreContentType(true);
			connect.ignoreHttpErrors(true);
			if (params != null) {
				connect.data(params);
			}
			if (headers != null) {
				connect.headers(headers);
			}
			connect.method(Method.POST);
			return connect.execute().body();
		} catch (Throwable e) {
		}
		return null;
	}

	public static String get(String url, Map<String, String> headers,
			Map<String, String> params) {
		try {
			Connection connect = Jsoup.connect(url);
			connect.ignoreContentType(true);
			connect.ignoreHttpErrors(true);
			if (params != null) {
				connect.data(params);
			}
			if (headers != null) {
				connect.headers(headers);
			}
			return connect.execute().body();
		} catch (Throwable e) {
		}
		return null;
	}

	public static void download(String url, String file) {
		try {
			Connection connect = org.jsoup.Jsoup.connect(url);
			connect.ignoreContentType(true);
			connect.ignoreHttpErrors(true);
			FileUtil.cpyStream(connect.execute().bodyStream(),
					new FileOutputStream(file));
		} catch (Throwable e) {
		}
	}

}
