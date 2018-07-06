/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsdroid.uiautomator2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.UiAutomation;
import android.app.UiAutomation.AccessibilityEventFilter;
import android.app.UiAutomation.OnAccessibilityEventListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.android.uiautomator.core.InstrumentationUiAutomatorBridge;
import com.jsdroid.node.Node;
import com.jsdroid.util.FileUtil;

/**
 * UiDevice provides access to state information about the device. You can also
 * use this class to simulate user actions on the device, such as pressing the
 * d-pad or pressing the Home and Menu buttons.
 * 
 * @since API Level 16
 */
@SuppressLint("NewApi")
public class UiDevice implements Searchable {
	private class DummyActivityController extends IActivityController.Stub {
		@Override
		public boolean activityResuming(String pkg) throws RemoteException {
			/* do nothing and let activity proceed normally */
			return true;
		}

		@Override
		public boolean activityStarting(Intent intent, String pkg)
				throws RemoteException {
			// try {
			// ComponentName component = intent.getComponent();
			// mStartActivity = component.getPackageName() + "/"
			// + component.getShortClassName();
			// } catch (Exception e) {
			// }
			return true;
		}

		@Override
		public boolean appCrashed(String processName, int pid, String shortMsg,
				String longMsg, long timeMillis, String stackTrace)
				throws RemoteException {
			/* do nothing and let activity proceed normally */
			return true;
		}

		@Override
		public int appEarlyNotResponding(String processName, int pid,
				String annotation) throws RemoteException {
			/* do nothing and let activity proceed normally */
			return 0;
		}

		@Override
		public int appNotResponding(String processName, int pid,
				String processStats) throws RemoteException {
			/* do nothing and let activity proceed normally */
			return 0;
		}

		@Override
		public int systemNotResponding(String message) throws RemoteException {
			/* do nothing and let system proceed normally */
			return 0;
		}
	}

	/**
	 * Proxy class which acts as an {@link AccessibilityEventFilter} and
	 * forwards calls to an {@link EventCondition} instance.
	 */
	private static class EventForwardingFilter implements
			AccessibilityEventFilter {
		private EventCondition<?> mCondition;

		public EventForwardingFilter(EventCondition<?> condition) {
			mCondition = condition;
		}

		@Override
		public boolean accept(AccessibilityEvent event) {
			// Guard against nulls
			return Boolean.TRUE.equals(mCondition.apply(event));
		}
	}

	private static final String LOG_TAG = UiDevice.class.getSimpleName();

	// Sometimes HOME and BACK key presses will generate no events if already on
	// home page or there is nothing to go back to, Set low timeouts.
	private static final long KEY_PRESS_EVENT_TIMEOUT = 1 * 1000;

	public static void init() {
		try {
			UiInit.main();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// store for registered UiWatchers
	private final HashMap<String, UiWatcher> mWatchers = new HashMap<String, UiWatcher>();

	private final List<String> mWatchersTriggers = new ArrayList<String>();

	// remember if we're executing in the context of a UiWatcher
	private boolean mInWatcherContext = false;

	// provides access the {@link QueryController} and {@link
	// InteractionController}
	private ShellUiAutomatorBridge mUiAutomationBridge;

	// Singleton instance
	private static UiDevice sInstance;

	// Get wait functionality from a mixin
	private WaitMixin<UiDevice> mWaitMixin = new WaitMixin<UiDevice>(this);
	private List<OnAccessibilityEventListener> onAccessibilityEventListeners = new ArrayList<OnAccessibilityEventListener>();
	/**
	 * A forward-looking API Level for development platform builds
	 * 
	 * This will be the actual API level on a released platform build, and will
	 * be last released API Level + 1 on development platform build
	 * 
	 * @hide
	 */
	static final int API_LEVEL_ACTUAL = Build.VERSION.SDK_INT
			+ ("REL".equals(Build.VERSION.CODENAME) ? 0 : 1);

	/**
	 * Retrieves a singleton instance of UiDevice
	 * 
	 * @return UiDevice instance
	 * @since API Level 16
	 */
	public static UiDevice getInstance() {
		if (sInstance == null) {
			sInstance = new UiDevice();
		}
		return sInstance;
	}

	private UiDevice() {
	}

	/**
	 * 快速模式，只获取重要的ui节点
	 * 
	 * @param flag
	 */
	public void setFastMode(boolean flag) {
		getAutomatorBridge().setFastMode(flag);
	}

	public boolean getFastMode() {
		return getAutomatorBridge().getFastMode();
	}

	/**
	 * 添加元素事件监听
	 * 
	 * @param onAccessibilityEventListener
	 */
	public void addAccessibilityEventListener(
			OnAccessibilityEventListener onAccessibilityEventListener) {
		synchronized (onAccessibilityEventListeners) {
			onAccessibilityEventListeners.add(onAccessibilityEventListener);
		}
	}

	/**
	 * 清空元素事件监听
	 */
	public void clearAccessibilityEventListener() {
		synchronized (onAccessibilityEventListeners) {
			onAccessibilityEventListeners.clear();
		}
	}

	/**
	 * 清除最后一次遍历得到的文本 {@link #getLastTraversedText()}.
	 * 
	 * @since API Level 16
	 */
	public void clearLastTraversedText() {
		Tracer.trace();
		getAutomatorBridge().getQueryController().clearLastTraversedText();
	}

	/**
	 * Perform a click at arbitrary coordinates specified by the user
	 * 
	 * @param x
	 *            coordinate
	 * @param y
	 *            coordinate
	 * @return true if the click succeeded else false
	 * @since API Level 16
	 */
	public boolean click(int x, int y) {
		Tracer.trace(x, y);
		if (x >= getDisplayWidth() || y >= getDisplayHeight()) {
			return (false);
		}
		return getAutomatorBridge().getInteractionController()
				.clickNoSync(x, y);
	}

	/**
	 * Performs a swipe from one coordinate to another coordinate. You can
	 * control the smoothness and speed of the swipe by specifying the number of
	 * steps. Each step execution is throttled to 5 milliseconds per step, so
	 * for a 100 steps, the swipe will take around 0.5 seconds to complete.
	 * 
	 * @param startX
	 *            X-axis value for the starting coordinate
	 * @param startY
	 *            Y-axis value for the starting coordinate
	 * @param endX
	 *            X-axis value for the ending coordinate
	 * @param endY
	 *            Y-axis value for the ending coordinate
	 * @param steps
	 *            is the number of steps for the swipe action
	 * @return true if swipe is performed, false if the operation fails or the
	 *         coordinates are invalid
	 * @since API Level 18
	 */
	public boolean drag(int startX, int startY, int endX, int endY, int steps) {
		Tracer.trace(startX, startY, endX, endY, steps);
		return getAutomatorBridge().getInteractionController().swipe(startX,
				startY, endX, endY, steps, true);
	}

	/**
	 * Dump the current window hierarchy to a {@link java.io.File}.
	 * 
	 * @param dest
	 *            The file in which to store the window hierarchy information.
	 * @throws IOException
	 */
	public void dumpWindowHierarchy(File dest) throws IOException {
		OutputStream stream = new BufferedOutputStream(new FileOutputStream(
				dest));
		try {
			dumpWindowHierarchy(new BufferedOutputStream(new FileOutputStream(
					dest)));
		} finally {
			stream.close();
		}
	}

	/**
	 * Dump the current window hierarchy to an {@link java.io.OutputStream}.
	 * 
	 * @param out
	 *            The output stream that the window hierarchy information is
	 *            written to.
	 * @throws IOException
	 */
	public void dumpWindowHierarchy(OutputStream out) throws IOException {
		AccessibilityNodeInfoDumper.dumpWindowHierarchy(this, out);
	}

	/**
	 * Executes a shell command using shell user identity, and return the
	 * standard output in string.
	 * <p>
	 * Calling function with large amount of output will have memory impacts,
	 * and the function call will block if the command executed is blocking.
	 * <p>
	 * Note: calling this function requires API level 21 or above
	 * 
	 * @param cmd
	 *            the command to run
	 * @return the standard output of the command
	 * @throws IOException
	 * @since API Level 21
	 * @hide
	 */
	public String executeShellCommand(String cmd) throws IOException {
		ParcelFileDescriptor pfd = mUiAutomationBridge.getUiAutomation()
				.executeShellCommand(cmd);
		byte[] buf = new byte[512];
		int bytesRead;
		FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
		StringBuffer stdout = new StringBuffer();
		while ((bytesRead = fis.read(buf)) != -1) {
			stdout.append(new String(buf, 0, bytesRead));
		}
		fis.close();
		return stdout.toString();
	}

	/** Returns the first object to match the {@code selector} criteria. */
	public UiObject2 findObject(BySelector selector) {
		try {
			AccessibilityNodeInfo node = ByMatcher.findMatch(this, selector,
					getWindowRoots());
			return node != null ? new UiObject2(this, selector, node) : null;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Returns a UiObject which represents a view that matches the specified
	 * selector criteria.
	 * 
	 * @param selector
	 * @return UiObject object
	 */
	public UiObject findObject(UiSelector selector) {
		return new UiObject(this, selector);
	}

	/** Returns all objects that match the {@code selector} criteria. */
	public List<UiObject2> findObjects(BySelector selector) {
		List<UiObject2> ret = new ArrayList<UiObject2>();
		for (AccessibilityNodeInfo node : ByMatcher.findMatches(this, selector,
				getWindowRoots())) {
			ret.add(new UiObject2(this, selector, node));
		}
		return ret;
	}

	/**
	 * Disables the sensors and freezes the device rotation at its current
	 * rotation state.
	 * 
	 * @throws RemoteException
	 * @since API Level 16
	 */
	public void freezeRotation() throws RemoteException {
		Tracer.trace();
		getAutomatorBridge().getInteractionController().freezeRotation();
	}

	/**
	 * 获取当前activity
	 * 
	 * @return
	 */
	public String getAct() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("/data/local/tmp/act");
			ServiceManager.getService(Context.ACTIVITY_SERVICE).dump(
					fout.getFD(), new String[] { "top" });
			String result = FileUtil.read("/data/local/tmp/act");
			String lines[] = result.split("\n");
			for (String line : lines) {
				if (line.contains("ACTIVITY") && line.contains("/")) {
					String sps[] = line.split(" ");
					for (String sp : sps) {
						if (sp.contains("/")) {
							return sp;
						}
					}
				}
			}
		} catch (Exception e) {
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
				}
			}
		}
		return "";
	}

	/**
	 * Provides access the {@link QueryController} and
	 * {@link InteractionController}
	 * 
	 * @return {@link InstrumentationUiAutomatorBridge}
	 */
	public ShellUiAutomatorBridge getAutomatorBridge() {
		if (mUiAutomationBridge == null) {
			throw new RuntimeException("UiDevice not initialized");
		}
		return mUiAutomationBridge;
	}

	/**
	 * Retrieves the last activity to report accessibility events.
	 * 
	 * @deprecated The results returned should be considered unreliable
	 * @return String name of activity
	 * @since API Level 16
	 */
	@Deprecated
	public String getCurrentActivityName() {
		Tracer.trace();
		return getAutomatorBridge().getQueryController()
				.getCurrentActivityName();
	}

	/**
	 * Retrieves the name of the last package to report accessibility events.
	 * 
	 * @return String name of package
	 * @since API Level 16
	 */
	public String getCurrentPackageName() {
		Tracer.trace();
		return getAutomatorBridge().getQueryController()
				.getCurrentPackageName();
	}

	/**
	 * Gets the height of the display, in pixels. The size is adjusted based on
	 * the current orientation of the display.
	 * 
	 * @return height in pixels or zero on failure
	 * @since API Level 16
	 */
	public int getDisplayHeight() {
		Tracer.trace();
		Display display = getAutomatorBridge().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		return p.y;
	}

	public int getDisplayUnRotationWidth() {
		int rotation = getDisplayRotation();
		if (rotation == UiAutomation.ROTATION_FREEZE_0
				|| rotation == UiAutomation.ROTATION_FREEZE_180) {
			return getDisplayWidth();
		} else {
			return getDisplayHeight();
		}
	}

	public int getDisplayUnRotationHeight() {
		int rotation = getDisplayRotation();
		if (rotation == UiAutomation.ROTATION_FREEZE_0
				|| rotation == UiAutomation.ROTATION_FREEZE_180) {
			return getDisplayHeight();
		} else {
			return getDisplayWidth();
		}
	}

	/**
	 * Returns the current rotation of the display, as defined in
	 * {@link Surface}
	 * 
	 * @since API Level 17
	 */
	public int getDisplayRotation() {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getRotation();
	}

	/**
	 * Returns the display size in dp (device-independent pixel)
	 * 
	 * The returned display size is adjusted per screen rotation. Also this will
	 * return the actual size of the screen, rather than adjusted per system
	 * decorations (like status bar).
	 * 
	 * @return a Point containing the display size in dp
	 */
	public Point getDisplaySizeDp() {
		Tracer.trace();
		Display display = getAutomatorBridge().getDefaultDisplay();
		Point p = new Point();
		display.getRealSize(p);
		DisplayMetrics metrics = new DisplayMetrics();
		display.getRealMetrics(metrics);
		float dpx = p.x / metrics.density;
		float dpy = p.y / metrics.density;
		p.x = Math.round(dpx);
		p.y = Math.round(dpy);
		return p;
	}

	/**
	 * Gets the width of the display, in pixels. The width and height details
	 * are reported based on the current orientation of the display.
	 * 
	 * @return width in pixels or zero on failure
	 * @since API Level 16
	 */
	public int getDisplayWidth() {
		Tracer.trace();
		Display display = getAutomatorBridge().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		return p.x;
	}

	/**
	 * Retrieves the text from the last UI traversal event received.
	 * 
	 * You can use this method to read the contents in a WebView container
	 * because the accessibility framework fires events as each text is
	 * highlighted. You can write a test to perform directional arrow presses to
	 * focus on different elements inside a WebView, and call this method to get
	 * the text from each traversed element. If you are testing a view container
	 * that can return a reference to a Document Object Model (DOM) object, your
	 * test should use the view's DOM instead.
	 * 
	 * @return text of the last traversal event, else return an empty string
	 * @since API Level 16
	 */
	public String getLastTraversedText() {
		Tracer.trace();
		return getAutomatorBridge().getQueryController().getLastTraversedText();
	}

	public List<Node> getNodes() {
		return AccessibilityNodeInfoDumper.dumpNodes(this);
	}

	/**
	 * 获取通知栏消息内容
	 * 
	 * @param pkg
	 * @return
	 */
	public String getNotification(String pkg) {
		// Manifest.permission.Acc
		StringBuilder datas = new StringBuilder();
		INotificationManager no = INotificationManager.Stub
				.asInterface(ServiceManager
						.getService(Context.NOTIFICATION_SERVICE));
		try {
			StatusBarNotification[] notifications = no
					.getActiveNotifications(pkg);
			for (int i = 0; i < notifications.length; i++) {
				Notification notification = notifications[i].getNotification();

				String text = null;
				if (notification.tickerText != null) {
					text = notification.tickerText.toString();
				}

				datas.append("tickerText:" + text + "\n");
				if (notification.extras != null) {
					for (String key : notification.extras.keySet()) {
						try {
							String value = (String) notification.extras
									.get(key);
							datas.append(key + ":" + value + "\n");
						} catch (Exception e) {
						}
					}
				}

			}
		} catch (RemoteException e) {
		}
		return datas.toString();
	}

	/**
	 * Retrieves the product name of the device.
	 * 
	 * This method provides information on what type of device the test is
	 * running on. This value is the same as returned by invoking #adb shell
	 * getprop ro.product.name.
	 * 
	 * @return product name of the device
	 * @since API Level 17
	 */
	public String getProductName() {
		Tracer.trace();
		return Build.PRODUCT;
	}

	public UiAutomation getUiAutomation() {
		return getAutomatorBridge().getUiAutomation();
	}

	/**
	 * Returns a list containing the root {@link AccessibilityNodeInfo}s for
	 * each active window
	 */
	public AccessibilityNodeInfo[] getWindowRoots() {
		ArrayList<AccessibilityNodeInfo> ret = new ArrayList<AccessibilityNodeInfo>();
		if (getAutomatorBridge().getFastMode()) {
			List<AccessibilityWindowInfo> windows = mUiAutomationBridge
					.getUiAutomation().getWindows();
			for (AccessibilityWindowInfo window : windows) {
				if (window != null)
					ret.add(window.getRoot());
			}
			if (ret.size() == 0) {
				ret.add(mUiAutomationBridge.getUiAutomation()
						.getRootInActiveWindow());
			}
		} else {
			ret.add(mUiAutomationBridge.getUiAutomation()
					.getRootInActiveWindow());
		}
		return ret.toArray(new AccessibilityNodeInfo[ret.size()]);
	}

	/**
	 * Checks if any registered {@link UiWatcher} have triggered.
	 * 
	 * See {@link #registerWatcher(String, UiWatcher)} See
	 * {@link #hasWatcherTriggered(String)}
	 * 
	 * @since API Level 16
	 */
	public boolean hasAnyWatcherTriggered() {
		Tracer.trace();
		return mWatchersTriggers.size() > 0;
	}

	/**
	 * Returns whether there is a match for the given {@code selector} criteria.
	 */
	public boolean hasObject(BySelector selector) {
		AccessibilityNodeInfo node = ByMatcher.findMatch(this, selector,
				getWindowRoots());
		if (node != null) {
			node.recycle();
			return true;
		}
		return false;
	}

	/**
	 * Checks if a specific registered {@link UiWatcher} has triggered. See
	 * {@link #registerWatcher(String, UiWatcher)}. If a UiWatcher runs and its
	 * {@link UiWatcher#checkForCondition()} call returned <code>true</code>,
	 * then the UiWatcher is considered triggered. This is helpful if a watcher
	 * is detecting errors from ANR or crash dialogs and the test needs to know
	 * if a UiWatcher has been triggered.
	 * 
	 * @param watcherName
	 * @return true if triggered else false
	 * @since API Level 16
	 */
	public boolean hasWatcherTriggered(String watcherName) {
		Tracer.trace(watcherName);
		return mWatchersTriggers.contains(watcherName);
	}

	public void initialize(ShellUiAutomatorBridge uiAutomatorBridge) {
		mUiAutomationBridge = uiAutomatorBridge;
		// 监听activity变化，用于getAct
		try {
			IActivityManager am = ActivityManagerNative.getDefault();
			am.setActivityController(new DummyActivityController());
		} catch (RemoteException e) {
		}
		UiAutomation uiAutomation = uiAutomatorBridge.getUiAutomation();
		uiAutomation
				.setOnAccessibilityEventListener(new OnAccessibilityEventListener() {
					public void onAccessibilityEvent(AccessibilityEvent event) {
						synchronized (onAccessibilityEventListeners) {
							for (OnAccessibilityEventListener listener : onAccessibilityEventListeners) {
								listener.onAccessibilityEvent(event);
							}
						}
					}
				});
	}

	boolean isInWatcherContext() {
		return mInWatcherContext;
	}

	/**
	 * Check if the device is in its natural orientation. This is determined by
	 * checking if the orientation is at 0 or 180 degrees.
	 * 
	 * @return true if it is in natural orientation
	 * @since API Level 17
	 */
	public boolean isNaturalOrientation() {
		Tracer.trace();
		waitForIdle();
		int ret = getAutomatorBridge().getRotation();
		return ret == UiAutomation.ROTATION_FREEZE_0
				|| ret == UiAutomation.ROTATION_FREEZE_180;
	}

	/**
	 * Checks the power manager if the screen is ON.
	 * 
	 * @return true if the screen is ON else false
	 * @throws RemoteException
	 * @since API Level 16
	 */
	public boolean isScreenOn() throws RemoteException {
		Tracer.trace();
		return getAutomatorBridge().getInteractionController().isScreenOn();
	}

	/**
	 * Opens the notification shade.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 18
	 */
	public boolean openNotification() {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getInteractionController()
				.openNotification();
	}

	/**
	 * Opens the Quick Settings shade.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 18
	 */
	public boolean openQuickSettings() {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getInteractionController()
				.openQuickSettings();
	}

	/**
	 * Performs the provided {@code action} and waits for the {@code condition}
	 * to be met.
	 * 
	 * @param action
	 *            The {@link Runnable} action to perform.
	 * @param condition
	 *            The {@link EventCondition} to evaluate.
	 * @param timeout
	 *            Maximum amount of time to wait in milliseconds.
	 * @return The final result returned by the condition.
	 */
	public <R> R performActionAndWait(Runnable action,
			EventCondition<R> condition, long timeout) {

		AccessibilityEvent event = null;
		try {
			event = getAutomatorBridge()
					.executeCommandAndWaitForAccessibilityEvent(action,
							new EventForwardingFilter(condition), timeout);
		} catch (TimeoutException e) {
			// Ignore
		}

		if (event != null) {
			event.recycle();
		}

		return condition.getResult();
	}

	/**
	 * Simulates a short press on the BACK button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressBack() {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getInteractionController()
				.sendKeyAndWaitForEvent(KeyEvent.KEYCODE_BACK, 0,
						AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
						KEY_PRESS_EVENT_TIMEOUT);
	}

	/**
	 * Simulates a short press on the DELETE key.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressDelete() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_DEL);
	}

	/**
	 * Simulates a short press on the CENTER button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressDPadCenter() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_DPAD_CENTER);
	}

	/**
	 * Simulates a short press on the DOWN button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressDPadDown() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_DPAD_DOWN);
	}

	/**
	 * Simulates a short press on the LEFT button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressDPadLeft() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_DPAD_LEFT);
	}

	/**
	 * Simulates a short press on the RIGHT button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressDPadRight() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_DPAD_RIGHT);
	}

	/**
	 * Simulates a short press on the UP button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressDPadUp() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_DPAD_UP);
	}

	/**
	 * Simulates a short press on the ENTER key.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressEnter() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_ENTER);
	}

	/**
	 * Simulates a short press on the HOME button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressHome() {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getInteractionController()
				.sendKeyAndWaitForEvent(KeyEvent.KEYCODE_HOME, 0,
						AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
						KEY_PRESS_EVENT_TIMEOUT);
	}

	/**
	 * Simulates a short press using a key code.
	 * 
	 * See {@link KeyEvent}
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressKeyCode(int keyCode) {
		Tracer.trace(keyCode);
		waitForIdle();
		return getAutomatorBridge().getInteractionController().sendKey(keyCode,
				0);
	}

	/**
	 * Simulates a short press using a key code.
	 * 
	 * See {@link KeyEvent}.
	 * 
	 * @param keyCode
	 *            the key code of the event.
	 * @param metaState
	 *            an integer in which each bit set to 1 represents a pressed
	 *            meta key
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressKeyCode(int keyCode, int metaState) {
		Tracer.trace(keyCode, metaState);
		waitForIdle();
		return getAutomatorBridge().getInteractionController().sendKey(keyCode,
				metaState);
	}

	/**
	 * Simulates a short press on the MENU button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressMenu() {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getInteractionController()
				.sendKeyAndWaitForEvent(KeyEvent.KEYCODE_MENU, 0,
						AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
						KEY_PRESS_EVENT_TIMEOUT);
	}

	/**
	 * Simulates a short press on the Recent Apps button.
	 * 
	 * @return true if successful, else return false
	 * @throws RemoteException
	 * @since API Level 16
	 */
	public boolean pressRecentApps() throws RemoteException {
		Tracer.trace();
		waitForIdle();
		return getAutomatorBridge().getInteractionController()
				.toggleRecentApps();
	}

	/**
	 * Simulates a short press on the SEARCH button.
	 * 
	 * @return true if successful, else return false
	 * @since API Level 16
	 */
	public boolean pressSearch() {
		Tracer.trace();
		return pressKeyCode(KeyEvent.KEYCODE_SEARCH);
	}

	/**
	 * Registers a {@link UiWatcher} to run automatically when the testing
	 * framework is unable to find a match using a {@link UiSelector}. See
	 * {@link #runWatchers()}
	 * 
	 * @param name
	 *            to register the UiWatcher
	 * @param watcher
	 *            {@link UiWatcher}
	 * @since API Level 16
	 */
	public void registerWatcher(String name, UiWatcher watcher) {
		Tracer.trace(name, watcher);
		if (mInWatcherContext) {
			throw new IllegalStateException(
					"Cannot register new watcher from within another");
		}
		mWatchers.put(name, watcher);
	}

	public void removeAccessibilityEventListener(
			OnAccessibilityEventListener onAccessibilityEventListener) {
		synchronized (onAccessibilityEventListeners) {
			onAccessibilityEventListeners.remove(onAccessibilityEventListener);
		}
	}

	/**
	 * Removes a previously registered {@link UiWatcher}.
	 * 
	 * See {@link #registerWatcher(String, UiWatcher)}
	 * 
	 * @param name
	 *            used to register the UiWatcher
	 * @since API Level 16
	 */
	public void removeWatcher(String name) {
		Tracer.trace(name);
		if (mInWatcherContext) {
			throw new IllegalStateException(
					"Cannot remove a watcher from within another");
		}
		mWatchers.remove(name);
	}

	/**
	 * Resets a {@link UiWatcher} that has been triggered. If a UiWatcher runs
	 * and its {@link UiWatcher#checkForCondition()} call returned
	 * <code>true</code>, then the UiWatcher is considered triggered. See
	 * {@link #registerWatcher(String, UiWatcher)}
	 * 
	 * @since API Level 16
	 */
	public void resetWatcherTriggers() {
		Tracer.trace();
		mWatchersTriggers.clear();
	}

	/**
	 * This method forces all registered watchers to run. See
	 * {@link #registerWatcher(String, UiWatcher)}
	 * 
	 * @since API Level 16
	 */
	public void runWatchers() {
		Tracer.trace();
		if (mInWatcherContext) {
			return;
		}

		for (String watcherName : mWatchers.keySet()) {
			UiWatcher watcher = mWatchers.get(watcherName);
			if (watcher != null) {
				try {
					mInWatcherContext = true;
					if (watcher.checkForCondition()) {
						setWatcherTriggered(watcherName);
					}
				} catch (Exception e) {
					Log.e(LOG_TAG, "Exceuting watcher: " + watcherName, e);
				} finally {
					mInWatcherContext = false;
				}
			}
		}
	}

	/**
	 * Enables or disables layout hierarchy compression.
	 * 
	 * If compression is enabled, the layout hierarchy derived from the
	 * Acessibility framework will only contain nodes that are important for
	 * uiautomator testing. Any unnecessary surrounding layout nodes that make
	 * viewing and searching the hierarchy inefficient are removed.
	 * 
	 * @param compressed
	 *            true to enable compression; else, false to disable
	 * @since API Level 18
	 */
	public void setCompressedLayoutHeirarchy(boolean compressed) {
		getAutomatorBridge().setFastMode(compressed);
	}

	/**
	 * Simulates orienting the device to the left and also freezes rotation by
	 * disabling the sensors.
	 * 
	 * If you want to un-freeze the rotation and re-enable the sensors see
	 * {@link #unfreezeRotation()}.
	 * 
	 * @throws RemoteException
	 * @since API Level 17
	 */
	public void setOrientationLeft() throws RemoteException {
		Tracer.trace();
		getAutomatorBridge().getInteractionController().setRotationLeft();
		waitForIdle(); // we don't need to check for idle on entry for this.
						// We'll sync on exit
	}

	/**
	 * Simulates orienting the device into its natural orientation and also
	 * freezes rotation by disabling the sensors.
	 * 
	 * If you want to un-freeze the rotation and re-enable the sensors see
	 * {@link #unfreezeRotation()}.
	 * 
	 * @throws RemoteException
	 * @since API Level 17
	 */
	public void setOrientationNatural() throws RemoteException {
		Tracer.trace();
		getAutomatorBridge().getInteractionController().setRotationNatural();
		waitForIdle(); // we don't need to check for idle on entry for this.
						// We'll sync on exit
	}

	/**
	 * Simulates orienting the device to the right and also freezes rotation by
	 * disabling the sensors.
	 * 
	 * If you want to un-freeze the rotation and re-enable the sensors see
	 * {@link #unfreezeRotation()}.
	 * 
	 * @throws RemoteException
	 * @since API Level 17
	 */
	public void setOrientationRight() throws RemoteException {
		Tracer.trace();
		getAutomatorBridge().getInteractionController().setRotationRight();
		waitForIdle(); // we don't need to check for idle on entry for this.
						// We'll sync on exit
	}

	/**
	 * Used internally by this class to set a {@link UiWatcher} state as
	 * triggered.
	 * 
	 * @param watcherName
	 */
	private void setWatcherTriggered(String watcherName) {
		Tracer.trace(watcherName);
		if (!hasWatcherTriggered(watcherName)) {
			mWatchersTriggers.add(watcherName);
		}
	}

	/**
	 * This method simply presses the power button if the screen is ON else it
	 * does nothing if the screen is already OFF.
	 * 
	 * @throws RemoteException
	 * @since API Level 16
	 */
	public void sleep() throws RemoteException {
		Tracer.trace();
		getAutomatorBridge().getInteractionController().sleepDevice();
	}

	/**
	 * Performs a swipe from one coordinate to another using the number of steps
	 * to determine smoothness and speed. Each step execution is throttled to
	 * 5ms per step. So for a 100 steps, the swipe will take about 1/2 second to
	 * complete.
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param steps
	 *            is the number of move steps sent to the system
	 * @return false if the operation fails or the coordinates are invalid
	 * @since API Level 16
	 */
	public boolean swipe(int startX, int startY, int endX, int endY, int steps) {
		Tracer.trace(startX, startY, endX, endY, steps);
		return getAutomatorBridge().getInteractionController().swipe(startX,
				startY, endX, endY, steps);
	}

	/**
	 * Performs a swipe between points in the Point array. Each step execution
	 * is throttled to 5ms per step. So for a 100 steps, the swipe will take
	 * about 1/2 second to complete
	 * 
	 * @param segments
	 *            is Point array containing at least one Point object
	 * @param segmentSteps
	 *            steps to inject between two Points
	 * @return true on success
	 * @since API Level 16
	 */
	public boolean swipe(Point[] segments, int segmentSteps) {
		Tracer.trace(segments, segmentSteps);
		return getAutomatorBridge().getInteractionController().swipe(segments,
				segmentSteps);
	}

	/**
	 * Take a screenshot of current window and store it as PNG
	 * 
	 * Default scale of 1.0f (original size) and 90% quality is used The
	 * screenshot is adjusted per screen rotation
	 * 
	 * @param storePath
	 *            where the PNG should be written to
	 * @return true if screen shot is created successfully, false otherwise
	 * @since API Level 17
	 */
	public boolean takeScreenshot(File storePath) {
		Tracer.trace(storePath);
		return takeScreenshot(storePath, 1.0f, 90);
	}

	/**
	 * Take a screenshot of current window and store it as PNG
	 * 
	 * The screenshot is adjusted per screen rotation
	 * 
	 * @param storePath
	 *            where the PNG should be written to
	 * @param scale
	 *            scale the screenshot down if needed; 1.0f for original size
	 * @param quality
	 *            quality of the PNG compression; range: 0-100
	 * @return true if screen shot is created successfully, false otherwise
	 * @since API Level 17
	 */
	public boolean takeScreenshot(File storePath, float scale, int quality) {
		Tracer.trace(storePath, scale, quality);
		return getAutomatorBridge().takeScreenshot(storePath, quality);
	}

	/**
	 * Re-enables the sensors and un-freezes the device rotation allowing its
	 * contents to rotate with the device physical rotation. During a test
	 * execution, it is best to keep the device frozen in a specific orientation
	 * until the test case execution has completed.
	 * 
	 * @throws RemoteException
	 */
	public void unfreezeRotation() throws RemoteException {
		Tracer.trace();
		getAutomatorBridge().getInteractionController().unfreezeRotation();
	}

	/**
	 * Waits for given the {@code condition} to be met.
	 * 
	 * @param condition
	 *            The {@link SearchCondition} to evaluate.
	 * @param timeout
	 *            Maximum amount of time to wait in milliseconds.
	 * @return The final result returned by the condition.
	 */
	public <R> R wait(SearchCondition<R> condition, long timeout) {
		return mWaitMixin.wait(condition, timeout);
	}

	/**
	 * Waits for the current application to idle. Default wait timeout is 10
	 * seconds
	 * 
	 * @since API Level 16
	 */
	public void waitForIdle() {
		Tracer.trace();
		waitForIdle(Configurator.getInstance().getWaitForIdleTimeout());
	}

	/**
	 * Waits for the current application to idle.
	 * 
	 * @param timeout
	 *            in milliseconds
	 * @since API Level 16
	 */
	public void waitForIdle(long timeout) {
		Tracer.trace(timeout);
		getAutomatorBridge().waitForIdle(timeout);
	}

	/**
	 * Waits for a window content update event to occur.
	 * 
	 * If a package name for the window is specified, but the current window
	 * does not have the same package name, the function returns immediately.
	 * 
	 * @param packageName
	 *            the specified window package name (can be <code>null</code>).
	 *            If <code>null</code>, a window update from any front-end
	 *            window will end the wait
	 * @param timeout
	 *            the timeout for the wait
	 * 
	 * @return true if a window update occurred, false if timeout has elapsed or
	 *         if the current window does not have the specified package name
	 * @since API Level 16
	 */
	public boolean waitForWindowUpdate(final String packageName, long timeout) {
		Tracer.trace(packageName, timeout);
		if (packageName != null) {
			if (!packageName.equals(getCurrentPackageName())) {
				return false;
			}
		}
		Runnable emptyRunnable = new Runnable() {
			@Override
			public void run() {
			}
		};
		AccessibilityEventFilter checkWindowUpdate = new AccessibilityEventFilter() {
			@Override
			public boolean accept(AccessibilityEvent t) {
				if (t.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
					return packageName == null
							|| packageName.equals(t.getPackageName());
				}
				return false;
			}
		};
		try {
			getAutomatorBridge().executeCommandAndWaitForAccessibilityEvent(
					emptyRunnable, checkWindowUpdate, timeout);
		} catch (TimeoutException e) {
			return false;
		} catch (Exception e) {
			Log.e(LOG_TAG,
					"waitForWindowUpdate: general exception from bridge", e);
			return false;
		}
		return true;
	}

	public String waitNotification(final String pkg, final long timeout) {
		final StringBuffer result = new StringBuffer();
		final Runnable emptyRunnable = new Runnable() {
			@Override
			public void run() {
			}
		};
		final UiAutomation.AccessibilityEventFilter checkWindowUpdate = (UiAutomation.AccessibilityEventFilter) new UiAutomation.AccessibilityEventFilter() {
			public boolean accept(final AccessibilityEvent event) {
				if (event.getEventType() == 64) {
					final String sourcePackageName = (String) event
							.getPackageName();
					if (sourcePackageName.equals(pkg)) {
						final Parcelable parcelable = event.getParcelableData();
						if (parcelable instanceof Notification) {
							final Notification notify = (Notification) parcelable;
							String tickerText = (String) notify.tickerText;
							if (notify.tickerText == null) {
								tickerText = "";
							}
							StatusBarNotification sbn = null;

							// NotificationManager nm = (NotificationManager)
							// ServiceManager.getService(Context.NOTIFICATION_SERVICE);
							result.append(tickerText);
							Bundle extras = null;
							try {
								extras = (Bundle) notify.getClass()
										.getField("extras").get(notify);
								for (final String key : extras.keySet()) {
									final Object value = extras.get(key);
									if (value != null) {
										result.append(String.valueOf(key) + "="
												+ value + "\n");
									}
								}
							} catch (Exception ex) {
							}
						}
						return true;
					}
				}
				return false;
			}
		};
		try {
			this.getAutomatorBridge()
					.executeCommandAndWaitForAccessibilityEvent(emptyRunnable,
							checkWindowUpdate, timeout);
		} catch (TimeoutException ex) {
		} catch (Exception ex2) {
		}
		return result.toString();
	}

	public String waitToast(final String pkg, final long timeout) {
		final StringBuffer result = new StringBuffer();
		final Runnable emptyRunnable = new Runnable() {
			@Override
			public void run() {
			}
		};
		final UiAutomation.AccessibilityEventFilter checkWindowUpdate = (UiAutomation.AccessibilityEventFilter) new UiAutomation.AccessibilityEventFilter() {
			public boolean accept(final AccessibilityEvent event) {
				if (event.getEventType() == 64) {
					final String sourcePackageName = (String) event
							.getPackageName();
					if (sourcePackageName.equals(pkg)) {
						final Parcelable parcelable = event.getParcelableData();
						if (!(parcelable instanceof Notification)) {
							final String toastMsg = (String) event.getText()
									.get(0);
							if (toastMsg != null) {
								result.append(toastMsg);
							}
							return true;
						}
					}
				}
				return false;
			}
		};
		try {
			this.getAutomatorBridge()
					.executeCommandAndWaitForAccessibilityEvent(emptyRunnable,
							checkWindowUpdate, timeout);
		} catch (TimeoutException ex) {
		} catch (Exception ex2) {
		}
		return result.toString();
	}

	/**
	 * This method simulates pressing the power button if the screen is OFF else
	 * it does nothing if the screen is already ON.
	 * 
	 * If the screen was OFF and it just got turned ON, this method will insert
	 * a 500ms delay to allow the device time to wake up and accept input.
	 * 
	 * @throws RemoteException
	 * @since API Level 16
	 */
	public void wakeUp() throws RemoteException {
		Tracer.trace();
		if (getAutomatorBridge().getInteractionController().wakeDevice()) {
			SystemClock.sleep(500);
		}
	}

	public int getRotation() {
		return getAutomatorBridge().getRotation();
	}

	public void unlock() {
		WindowManager s = null;
		KeyguardManager km = null;
		km = (KeyguardManager) getAutomatorBridge().getContext().getSystemService(
				Context.KEYGUARD_SERVICE);
		km.newKeyguardLock("StartupReceiver").disableKeyguard();
		
	}
}
