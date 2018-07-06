package com.jsdroid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	public static byte[] gzip(byte[] data) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(out);

		zip.write(data);
		zip.close();
		out.close();
		byte[] result = out.toByteArray();
		return result;
	}

	public static byte[] unGzip(byte[] data) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPInputStream ungzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = ungzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		in.close();
		ungzip.close();
		return out.toByteArray();
	}
	public static void main(String[] args) {
	}

}
