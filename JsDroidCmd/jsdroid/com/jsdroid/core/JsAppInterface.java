package com.jsdroid.core;

public interface JsAppInterface {
	// 输出到命令行
	public void print(String obj);

	// 弹出toast消息
	public void toast(String obj);

	// 模拟输入
	public void input(String obj);

	// 打印日志
	public void log(String obj);
}
