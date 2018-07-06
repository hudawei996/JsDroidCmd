package com.jsdroid.core;

public class JsCmd {
	// 运行脚本
	public static final int TYPE_RUN = 0;
	// 截图
	public static final int TYPE_CAPTURE = 1;
	// 结果
	public static final int TYPE_RESULT = 2;
	// 消息
	public static final int TYPE_TOAST = 3;
	// 输出
	public static final int TYPE_PRINT = 4;
	// 日志
	public static final int TYPE_LOG = 5;
	// 输入
	public static final int TYPE_INPUT = 6;
	// 录制
	public static final int TYPE_RECORD = 7;
	// 关闭服务
	public static final int TYPE_CLOSE = 8;
	// 停止录制
	public static final int TYPE_STOP_RECORD = 9;
	// 音量键按下
	public static final int TYPE_VOLUME_DOWN = 10;
	// 停止
	public static final int TYPE_STOP_SCRIPT = 11;
	// 电脑截屏
	public static final int TYPE_CAPTURE_TCP = 12;
	// 重启
	public static final int TYPE_RESTART = 13;

	public int type;
	public String data;
	public boolean isSend;
	public int line;
	public String sourceName;
}
