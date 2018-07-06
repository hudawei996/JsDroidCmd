package com.jsdroid.bean;

public class DeviceInfo {

	// 安卓sdk版本
	public int sdkVersion;
	// 设备型号
	public String deviceModel;
	// 设备安卓版本
	public String deviceVersion;
	// 设备厂商
	public String deviceBrand;
	// 是否root
	public boolean hasRoot;
	// 是否成功添加系统服务
	public boolean canAddSystemService;
	// 是否成功启动uiautomator服务
	public boolean canConnectUiautomation;
	// 是否成功加载so库
	public boolean loadSo;
	// 加载了哪种类型的so库
	public String cpu_load;
	// 是否支持localSocket
	public boolean canUseLocalSocket;
	// 是否支持socket连接
	public boolean canUseSocket;
	// 支持的cpu1
	public String cpu_abi;
	// 支持的cpu2
	public String cpu_abi2;
	// 支持的cpu列表
	public String[] supported_abis;
	// 是否能够读写data/local
	public boolean canWriteLocal;
	// 是否能够读写/data/local/tmp
	public boolean canWriteLocalTmp;
	// 是否能够读写sdcard
	public boolean canWriteSdcard;
	// 是否可以获取系统Context
	public boolean canCreateContext;
}
