package com.jsdroid.core;

import java.util.Map;

public class JsUserCase {
	public static final int TYPE_TEXT = 0x00;
	public static final int TYPE_FILE = 0x01;
	public static final int TYPE_DIRECTORY = 0x02;
	// 类型
	int type;
	// 脚本内容：当脚本类型为文字时有效
	String source;
	// 文件名：当脚本类型为文件时有效
	String filename;
	// 目录：当脚本类型为目录时有效
	String directory;
	// 配置
	Map<String, String> options;
	// 包名
	String pkg;

	public String getDirectory() {
		return directory;
	}

	public String getFilename() {
		return filename;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public String getPkg() {
		return pkg;
	}

	public String getSource() {
		return source;
	}

	public int getType() {
		return type;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setType(int type) {
		this.type = type;
	}

}
