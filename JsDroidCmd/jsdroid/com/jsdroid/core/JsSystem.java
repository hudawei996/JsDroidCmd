package com.jsdroid.core;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

import org.mozilla.javascript.Context;

import android.app.ActivityThread;
import android.app.UiAutomation;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.jsdroid.bean.DeviceInfo;
import com.jsdroid.event.JsDroidEvent;
import com.jsdroid.uiautomator2.Configurator;
import com.jsdroid.uiautomator2.ShellUiAutomatorBridge;
import com.jsdroid.uiautomator2.UiAutomationShellWrapper;
import com.jsdroid.uiautomator2.UiDevice;
import com.jsdroid.util.FileUtil;
import com.jsdroid.util.HttpUtil;
import com.jsdroid.util.LibraryUtil;
import com.jsdroid.util.ShellUtil;

import eu.chainfire.libsuperuser.Shell;

public class JsSystem {
	public static String DEX_DIR = "/data/local/tmp/app_dex";
	public static String LIB_DIR = "/data/local/tmp/app_lib";
	public static DeviceInfo deviceInfo = new DeviceInfo();

	public static void main() {
		// 启动系统服务，用于判断是否已经启动，如果已经启动，则退出程序
		systemService();
		// 启动系统服务
		systeMain();
		// 启动local socket服务
		new JsLocalServer();

		// 启动socket服务
		new JsSocketServer();

		// 连接无障碍服务
		connectUiautomation();

		// 判断是否具有root权限
		int uid = android.os.Process.myUid();
		if (uid == 0) {
			deviceInfo.hasRoot = true;
			JsSystem.DEX_DIR = "/data/local/app_dex";
			JsSystem.LIB_DIR = "/data/local/app_lib";
		}
		Shell.SH.run("mkdir " + JsSystem.DEX_DIR);
		Shell.SH.run("mkdir " + JsSystem.LIB_DIR);

		// 加载全部so库
		boolean loadok = LibraryUtil.loadAllLibrary(JsSystem.LIB_DIR);
		if (loadok) {
			// 初始化JsDroidEvent，用于监听录制
			JsDroidEvent.init();
			// 启动event线程，用于录制或者监听音量键
			JsEvent.getInstance();
			deviceInfo.loadSo = true;
		}

		// 启动http服务调用
		new JsHttpServer();

		// 启动runjs服务
		new JsRunjsServer();

		// 启动截图服务
		new JsCaptureServer();

		// 保存设备信息
		saveDeviceInfo();

	}

	static void systeMain() {
		try {
			ActivityThread.systemMain();
		} catch (Throwable e) {
		}
		try {
			deviceInfo.canCreateContext = ActivityThread
					.currentActivityThread().getApplication() != null;
		} catch (Throwable e) {
		}
	}

	static void systemService() {
		try {
			IBinder service = ServiceManager.getService("jsdroid_service");
			if (service != null) {
				System.out.println("aready started");
				System.exit(0);
			}
		} catch (Throwable e) {
		}
		try {
			ServiceManager.addService("jsdroid_service", new IJsService.Stub() {
				@Override
				protected void dump(FileDescriptor fd, PrintWriter fout,
						String[] args) {
					try {
						String code = FileUtil.read(args[0]);
						JsUserCase jsUseCase = new JsUserCase();
						jsUseCase.type = JsUserCase.TYPE_TEXT;
						jsUseCase.source = code;
						JsThread thread = new JsThread(jsUseCase, JsGlobal
								.getGlobal());
						thread.start();
						JsResult result = null;
						try {
							result = thread.waitResult();
						} catch (InterruptedException e) {
						}
						if (result != null) {
							result.result = Context.toString(result.result);
							fout.println(result.result);
						}
					} catch (Throwable e) {
						fout.println("err");
					}
				}
			});
			// 成功添加系统服务
			deviceInfo.canAddSystemService = true;
		} catch (Throwable e) {
		}
	}

	static void connectUiautomation() {
		try {
			// 配置空闲等待时间
			Configurator.getInstance().setWaitForIdleTimeout(0);
			// 配置空闲等待间隔
			Configurator.getInstance().setWaitForSelectorTimeout(0);
			// 连接uiautomation服务
			UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
			automationWrapper.connect();
			UiAutomation uiAutomation = automationWrapper.getUiAutomation();
			// 初始化UiDevice
			UiDevice.getInstance().initialize(
					new ShellUiAutomatorBridge(uiAutomation));
			deviceInfo.canConnectUiautomation = true;
		} catch (Throwable e) {
		}
	}

	static void saveDeviceInfo() {
		deviceInfo.deviceBrand = android.os.Build.BRAND;
		deviceInfo.deviceVersion = android.os.Build.VERSION.RELEASE;
		deviceInfo.deviceModel = android.os.Build.MODEL;
		deviceInfo.sdkVersion = android.os.Build.VERSION.SDK_INT;
		deviceInfo.cpu_abi = android.os.Build.CPU_ABI;
		deviceInfo.cpu_abi2 = android.os.Build.CPU_ABI2;
		deviceInfo.supported_abis = android.os.Build.SUPPORTED_ABIS;
		deviceInfo.canWriteLocal = FileUtil.canWriteLocal();
		deviceInfo.canWriteLocalTmp = FileUtil.canWriteLocalTmp();
		deviceInfo.canWriteSdcard = FileUtil.canWriteSdcard();
		String info = JSON.toJSONString(deviceInfo);
		
		// 保存到tmp
		try {
			FileUtil.write("/data/local/tmp/deviceInfo.txt", info);
		} catch (Exception e) {
		}
		// 保存到sdcard
		try {
			FileUtil.write("/sdcard/deviceInfo.txt", info);
		} catch (Exception e) {
		}
	}

	public static String readCpuInfo() {
		try {
			return ShellUtil.exec("cat /proc/cpuinfo").toLowerCase();
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * 判断蓝牙是否有效来判断是否为模拟器
	 * 
	 * @return true 为模拟器
	 */
	public static boolean notHasBlueTooth() {
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (ba == null) {
			return true;
		} else {
			// 如果有蓝牙不一定是有效的。获取蓝牙名称，若为null 则默认为模拟器
			String name = ba.getName();
			if (TextUtils.isEmpty(name)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 判断是否为模拟器
	 * 
	 * @return
	 */
	public static boolean isFeatures() {
		return Build.FINGERPRINT.startsWith("generic")
				|| Build.FINGERPRINT.toLowerCase().contains("vbox")
				|| Build.FINGERPRINT.toLowerCase().contains("test-keys")
				|| Build.MODEL.contains("google_sdk")
				|| Build.MODEL.contains("Emulator")
				|| Build.MODEL.contains("Android SDK built for x86")
				|| Build.MANUFACTURER.contains("Genymotion")
				|| (Build.BRAND.startsWith("generic") && Build.DEVICE
						.startsWith("generic"))
				|| "google_sdk".equals(Build.PRODUCT);
	}

	/**
	 * 判断cpu是否为电脑来判断 模拟器
	 * 
	 * @return true 为模拟器
	 */
	public static boolean checkIsNotRealPhone() {
		String cpuInfo = readCpuInfo();
		if (cpuInfo == null) {
			return true;
		}
		if ((cpuInfo.contains("intel") || cpuInfo.contains("amd"))) {
			return true;
		}
		return false;
	}

}
