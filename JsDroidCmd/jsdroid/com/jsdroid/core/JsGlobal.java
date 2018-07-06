package com.jsdroid.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Interpreter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import android.graphics.Bitmap;

import com.jsdroid.config.IConfigService;
import com.jsdroid.event.JsDroidEvent;
import com.jsdroid.findimg.FindImg;
import com.jsdroid.findpic.FindPic;
import com.jsdroid.input.Input;
import com.jsdroid.proxy.ProxyServiceManager;
import com.jsdroid.uiautomator2.BySelector;
import com.jsdroid.uiautomator2.UiDevice;
import com.jsdroid.util.BitmapUtil;
import com.jsdroid.util.FileUtil;

import dalvik.system.DexClassLoader;

public class JsGlobal extends ImporterTopLevel {
	private static JsGlobal instance = new JsGlobal();;

	
	public static JsGlobal getGlobal() {
		return instance;
	}

	class ClearImageFunction extends BaseFunction {
		private static final long serialVersionUID = 4007782597757947582L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {

			clearImages();
			return super.call(cx, scope, thisObj, args);
		}
	}

	class DeleteScreenFunction extends BaseFunction {

		private static final long serialVersionUID = -8323127787681608794L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			deleteScreen();
			return super.call(cx, scope, thisObj, args);
		}
	}

	class FindImgFunction extends BaseFunction {

		private static final long serialVersionUID = 3191903765576559272L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length == 9) {
				String imgFile = Context.toString(args[0]);
				double left = Context.toNumber(args[1]);
				double top = Context.toNumber(args[2]);
				double right = Context.toNumber(args[3]);
				double bottom = Context.toNumber(args[4]);
				double level = Context.toNumber(args[5]);
				double distance = Context.toNumber(args[6]);
				double offset = Context.toNumber(args[7]);
				double sim = Context.toNumber(args[8]);
				Bitmap screen = readScreen();
				return FindImg
						.findImg(screen, readImg(imgFile, scriptDir),
								(int) level, (int) left, (int) top,
								(int) right, (int) bottom, (int) offset,
								(int) distance, (float) sim);

			}
			if (args.length == 11) {
				double width = Context.toNumber(args[0]);
				double height = Context.toNumber(args[1]);
				String imgFile = Context.toString(args[2]);
				double left = Context.toNumber(args[3]);
				double top = Context.toNumber(args[4]);
				double right = Context.toNumber(args[5]);
				double bottom = Context.toNumber(args[6]);
				double level = Context.toNumber(args[7]);
				double distance = Context.toNumber(args[8]);
				double offset = Context.toNumber(args[9]);
				double sim = Context.toNumber(args[10]);
				Bitmap screen = readScreen((int) width, (int) height);
				return FindImg
						.findImg(screen, readImg(imgFile, scriptDir),
								(int) level, (int) left, (int) top,
								(int) right, (int) bottom, (int) offset,
								(int) distance, (float) sim);
			}
			return super.call(cx, scope, thisObj, args);
		}
	}

	class FindPicFunction extends BaseFunction {
		private static final long serialVersionUID = 8155995979912788840L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length == 7) {
				Bitmap screen = readScreen();
				String imgFile = Context.toString(args[0]);
				double left = Context.toNumber(args[1]);
				double top = Context.toNumber(args[2]);
				double right = Context.toNumber(args[3]);
				double bottom = Context.toNumber(args[4]);
				double offset = Context.toNumber(args[5]);
				double sim = Context.toNumber(args[6]);
				return FindPic.findPic(screen, readImg(imgFile, scriptDir),
						(int) left, (int) top, (int) right, (int) bottom,
						(int) offset, (float) sim);
			}
			if (args.length == 9) {

				double width = Context.toNumber(args[0]);
				double height = Context.toNumber(args[1]);
				Bitmap screen = readScreen((int) width, (int) height);
				String imgFile = Context.toString(args[2]);
				double left = Context.toNumber(args[3]);
				double top = Context.toNumber(args[4]);
				double right = Context.toNumber(args[5]);
				double bottom = Context.toNumber(args[6]);
				double offset = Context.toNumber(args[7]);
				double sim = Context.toNumber(args[8]);
				return FindPic.findPic(screen, readImg(imgFile, scriptDir),
						(int) left, (int) top, (int) right, (int) bottom,
						(int) offset, (float) sim);
			}
			return super.call(cx, scope, thisObj, args);
		}
	}

	class FindViewFunction extends BaseFunction {

		private static final long serialVersionUID = 4779360473092481343L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			BySelector by = (BySelector) Context.jsToJava(args[0],
					BySelector.class);
			Object ret = UiDevice.getInstance().findObject(by);
			if (ret != null) {
				return ret;
			}
			return Undefined.instance;
		}
	}

	class FindViewsFunction extends BaseFunction {

		private static final long serialVersionUID = 3205359764168750009L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			BySelector by = (BySelector) Context.jsToJava(args[0],
					BySelector.class);
			Object ret = UiDevice.getInstance().findObjects(by);
			if (ret != null) {
				return ret;
			}
			return Undefined.instance;
		}
	}

	class IncludeFucntion extends BaseFunction {
		private static final long serialVersionUID = 423537772747645160L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {

			String source = null;
			String sourceName = null;
			String url = Context.toString(args[0]);
			sourceName = url;
			source = JsInclude.readSource(url, scriptDir);
			if (source != null) {
				try {
					cx.evaluateString(scope, source, sourceName, 1, null);
				} catch (Throwable e) {
					return "include err";
				}
			} else {
				// 加载类
				try {
					ClassLoader classLoader = JsGlobal.class.getClassLoader();
					String name = sourceName.replace("/", "_")
							.replace(".", "_");
					Class<?> _class = classLoader
							.loadClass("com.jsdroid.native." + name);
					Method execMethod = _class.getMethod("exec", Context.class,
							Scriptable.class);
					execMethod.invoke(_class.newInstance(), cx, scope);
				} catch (Throwable e) {
					try {
						e.printStackTrace(new PrintStream(new File(
								"/data/local/tmp/log")));
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					return "include err";
				}
			}
			return super.call(cx, scope, thisObj, args);
		}
	}

	class InputFunction extends BaseFunction {
		private static final long serialVersionUID = 7796079488851596418L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			String obj = Context.toString(args[0]);
			if (getJsAppInterface() != null) {
				getJsAppInterface().input(obj);
			}
			return Input.input(obj);
		}
	}

	class LoadApkFunction extends BaseFunction {

		private static final long serialVersionUID = 3432554015470833832L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			File apkFile = getFile(Context.toString(args[0]));
			if (apkFile != null) {
				return loadapk(apkFile);
			}
			return false;
		}
	}

	class LogFunction extends BaseFunction {
		private static final long serialVersionUID = -5379010219127288856L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (getJsAppInterface() != null) {
				getJsAppInterface().log(Context.toString(args[0]));
			}
			File file = new File(scriptDir, "log");
			FileUtil.append(file.getPath(), Context.toString(args[0]));
			return super.call(cx, scope, thisObj, args);
		}
	}

	class PrintFunction extends BaseFunction {
		private static final long serialVersionUID = -8744466305310872021L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			String obj = Context.toString(args[0]);
			if (getJsAppInterface() != null) {
				getJsAppInterface().print(obj);
			}
			File file = new File(scriptDir, "log");
			FileUtil.append(file.getPath(), Context.toString(args[0]));
			return super.call(cx, scope, thisObj, args);
		}
	}

	class SaveScreenFunction extends BaseFunction {
		private static final long serialVersionUID = 4007782597757947582L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length == 2) {
				double width = Context.toNumber(args[0]);
				double height = Context.toNumber(args[1]);
				saveScreen((int) width, (int) height);
			} else {
				saveScreen();
			}
			return super.call(cx, scope, thisObj, args);
		}
	}

	class QuitFunction extends BaseFunction {

		private static final long serialVersionUID = 8043572079687355139L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			quit();
			return Undefined.instance;
		}
	}

	class ScreenshotFunction extends BaseFunction {
		private static final long serialVersionUID = 1397841078120405007L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			UiDevice.getInstance().takeScreenshot(
					new File(Context.toString(args[0])));
			return Undefined.instance;
		}
	}

	class SleepFunction extends BaseFunction {
		private static final long serialVersionUID = 1397841078120405007L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			double num = Context.toNumber(args[0]);
			try {
				Thread.sleep((long) num);
			} catch (InterruptedException e) {
			}
			return super.call(cx, scope, thisObj, args);
		}
	}

	class TimeFunction extends BaseFunction {
		private static final long serialVersionUID = 1397841078120405007L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			return System.currentTimeMillis();
		}
	}

	class ToastFunction extends BaseFunction {
		private static final long serialVersionUID = 1397841078120405007L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (getJsAppInterface() != null) {
				getJsAppInterface().toast(Context.toString(args[0]));
			}
			return super.call(cx, scope, thisObj, args);
		}
	}

	class XmlFunction extends BaseFunction {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				UiDevice.getInstance().dumpWindowHierarchy(out);
			} catch (IOException e) {
			}
			try {
				out.close();
			} catch (IOException e) {
			}
			return new String(out.toByteArray());
		}
	}

	class ReadConfigFunction extends BaseFunction {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			String key = Context.toString(args[0]);
			try {
				IConfigService config = ProxyServiceManager.getService(pkg
						+ ".config", IConfigService.class);
				if (config != null) {
					Object ret = config.read(key);
					if (ret != null) {
						return ret.toString();
					}
				}
			} catch (Exception e) {
			}
			return Undefined.instance;
		}
	}

	class SaveConfigFunction extends BaseFunction {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			try {
				String key = Context.toString(args[0]);
				String value = Context.toString(args[1]);
				IConfigService config = ProxyServiceManager.getService(pkg
						+ ".config", IConfigService.class);
				if (config != null) {
					config.save(key, value);
				}
			} catch (Exception e) {
			}
			return Undefined.instance;
		}
	}

	private static List<String> apkFileCache = new ArrayList<String>();

	private static Map<String, Bitmap> imgCache = new LinkedHashMap<String, Bitmap>();

	private static Bitmap screenImg;

	private static final long serialVersionUID = 7197662993041453531L;

	// 清除缓存图片
	public static synchronized void clearImages() {
		imgCache.clear();
	}

	// 删除截屏
	public static synchronized void deleteScreen() {
		if (screenImg != null)
			screenImg.recycle();
		screenImg = null;
	}

	public static boolean loadapk(File apkFile) {
		if (apkFileCache.contains(apkFile.getPath())) {
			return true;
		}
		apkFileCache.add(apkFile.getPath());
		File dexFile = null;
		dexFile = new File(JsSystem.DEX_DIR);
		if (!dexFile.exists()) {
			dexFile.mkdir();
		}
		final ClassLoader classLoader = JsGlobal.class.getClassLoader();
		final ClassLoader parent = classLoader.getParent();
		DexClassLoader dexClassLoader = new DexClassLoader(apkFile.getPath(),
				dexFile.getPath(), "", parent) {
			@Override
			public Class<?> loadClass(String className)
					throws ClassNotFoundException {
				try {
					return classLoader.loadClass(className);
				} catch (Exception e) {
				}
				return super.loadClass(className);
			}
		};
		try {
			Field parentField = ClassLoader.class.getDeclaredField("parent");
			parentField.setAccessible(true);
			parentField.set(classLoader, dexClassLoader);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	// 读取图片
	private static synchronized Bitmap readImg(String file, String scriptDir) {
		Bitmap readImg = null;
		if (imgCache.containsKey(file)) {
			readImg = imgCache.get(file);
		} else {
			readImg = BitmapUtil.read(file, scriptDir);
			imgCache.put(file, readImg);
			releaseImgChache();
		}
		return readImg;
	}

	// 截屏
	private synchronized static Bitmap readScreen() {
		Bitmap readImg = null;
		if (screenImg != null) {
			readImg = screenImg;
		} else {
			readImg = BitmapUtil.takeScreenshot();
		}
		return readImg;
	}

	// 截屏
	private synchronized static Bitmap readScreen(int width, int height) {
		Bitmap readImg = null;
		if (screenImg != null && width == screenImg.getWidth()
				&& height == screenImg.getHeight()) {
			readImg = screenImg;
		} else {
			readImg = BitmapUtil.takeScreenshot(width, height);
		}
		return readImg;
	}

	// 清理图片，如果图片内存超过10m，从第一个清理
	public static synchronized void releaseImgChache() {
		long memorySize = 0;
		for (Bitmap pic : imgCache.values()) {
			memorySize += pic.getWidth() * 4 * pic.getHeight();
		}
		if (memorySize > 1024 * 1024 * 10) {
			Iterator<String> iterator = imgCache.keySet().iterator();
			Bitmap remove = imgCache.remove(iterator.next());
			remove.recycle();
			releaseImgChache();
		}
	}

	// 锁定截屏
	public static synchronized void saveScreen() {
		screenImg = BitmapUtil.takeScreenshot();
	}

	// 锁定截屏
	public static synchronized void saveScreen(int width, int height) {
		screenImg = BitmapUtil.takeScreenshot((int) width, (int) height);
	}

	private JsAppInterface jsAppInterface;

	public String pkg = "com.jsdroid";

	public String scriptDir;
	private Context context;

	private JsGlobal() {
		this(Context.enter());
	}

	private JsGlobal(Context cx) {
		super(cx);
		context = cx;
		// 添加自己的代码
		init(cx);

	}

	public File getFile(String filename) {
		File file1 = new File(filename);
		if (file1.exists()) {
			return file1;
		}
		File file2 = new File(scriptDir, filename);
		if (file2.exists()) {
			return file2;
		}
		return null;
	}

	public JsAppInterface getJsAppInterface() {
		return jsAppInterface;
	}

	private void init(Context cx) {
		cx.setOptimizationLevel(-1);
		// 添加bug
		cx.setDebugger(JsDebug.getInstance(), null);
		putConst("device", this, UiDevice.getInstance());
		putConst("event", this, new JsDroidEvent());
		putConst("include", this, new IncludeFucntion());
		putConst("loadapk", this, new LoadApkFunction());
		putConst("print", this, new PrintFunction());
		putConst("toast", this, new ToastFunction());
		putConst("input", this, new InputFunction());
		putConst("log", this, new LogFunction());
		putConst("findView", this, new FindViewFunction());
		putConst("findViews", this, new FindViewsFunction());
		putConst("findPic", this, new FindPicFunction());
		putConst("findImg", this, new FindImgFunction());
		putConst("time", this, new TimeFunction());
		putConst("sleep", this, new SleepFunction());
		putConst("saveScreen", this, new SaveScreenFunction());
		putConst("deleteScreen", this, new DeleteScreenFunction());
		putConst("clearImages", this, new ClearImageFunction());
		putConst("getXml", this, new XmlFunction());
		putConst("screenshot", this, new ScreenshotFunction());
		putConst("quit", this, new QuitFunction());
		putConst("config", this, Config.getInstance());
		// putConst("saveConfig", this, new SaveConfigFunction());
		// 加载mqm.js
		cx.evaluateString(this, "include('assets/mqm.js')", "<init>", 1, null);
	}

	public void setJsAppInterface(JsAppInterface jsAppInterface) {
		this.jsAppInterface = jsAppInterface;
	}

	public void enter() {
		Interpreter.enter();
	}

	public void quit() {
		Interpreter.quit();
	}

}
