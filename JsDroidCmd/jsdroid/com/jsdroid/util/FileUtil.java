package com.jsdroid.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class FileUtil {
	public static boolean canWriteLocal() {
		return canWrite("/data/local/testLocal.txt");
	}

	public static boolean canWriteLocalTmp() {
		return canWrite("/data/local/tmp/testLocal.txt");
	}

	public static boolean canWriteSdcard() {
		return canWrite("/sdcard/testLocal.txt");
	}

	public static boolean canWrite(String filename) {
		try {
			FileUtil.write(filename, "ok");
			String ret = FileUtil.read(filename);
			new File(filename).delete();
			if (ret != null && ret.contains("ok")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static byte[] readBytes(String filename) {
		File file = new File(filename);
		return readBytes(file);
	}

	public static void writeBytes(String filename, byte[] data) {
		File file = new File(filename);
		writeBytes(file, data);
	}

	public static void writeBytes(File file, byte[] data) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(data);
		} catch (Exception e) {
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	public static byte[] readBytes(File file) {
		if (!file.exists()) {
			return null;
		}
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			in = new FileInputStream(file);
			cpyStream(in, out);
		} catch (Exception e) {
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return out.toByteArray();

	}

	public static byte[] readBytes(InputStream in) {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			cpyStream(in, out);
		} catch (Exception e) {
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return out.toByteArray();

	}

	public static void append(String file, String content) {
		try {
			RandomAccessFile rf = new RandomAccessFile(file, "rw");
			rf.seek(rf.length());
			rf.write((content + "\r\n").getBytes("utf-8"));
			rf.close();
		} catch (Exception e) {
		}
	}

	public static void cpyStream(InputStream in, OutputStream out)
			throws IOException {
		byte buff[] = new byte[1024];
		int len = 0;
		while ((len = in.read(buff)) != -1) {
			out.write(buff, 0, len);
		}
		out.close();
		in.close();
	}

	public static void createDir(String filename) {
		File file = new File(filename).getAbsoluteFile();
		if (file.isDirectory()) {
			file.mkdirs();
		} else {
			File parentFile = file.getParentFile();
			if (parentFile != null) {
				parentFile.mkdirs();
			}
		}
	}

	public static String read(File file) {
		if (!file.exists()) {
			return null;
		}
		try {
			return read(new FileInputStream(file));
		} catch (Exception e) {
		}
		return null;
	}

	public static String read(InputStream in) {
		BufferedReader reader = null;
		StringBuffer result = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			String line = null;
			result = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			return result.toString().trim();
		} catch (Exception e) {
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static String read(String filename) {
		return read(new File(filename));
	}

	public static void write(String filename, String content) {
		createDir(filename);
		write(new File(filename), content);

	}

	public static void write(File file, String content) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(content.getBytes("utf-8"));
		} catch (Exception e) {
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	public static void main(String[] args) {
		File file = new File("/sss/sss/");
		System.out.println(file.getParentFile());
	}

}
