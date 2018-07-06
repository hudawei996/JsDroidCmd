/*
 * Copyright (C) 2013 The Android Open Source Project
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.UiAutomation;
import android.app.UiAutomation.AccessibilityEventFilter;
import android.app.UiAutomation.OnAccessibilityEventListener;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * @hide
 */
@SuppressLint("NewApi")
public abstract class UiAutomatorBridge {

	private static final String LOG_TAG = UiAutomatorBridge.class
			.getSimpleName();

	/**
	 * This value has the greatest bearing on the appearance of test execution
	 * speeds. This value is used as the minimum time to wait before considering
	 * the UI idle after each action.
	 */
	private static final long QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE = 500;// ms

	/**
	 * This is the maximum time the automation will wait for the UI to go idle.
	 * Execution will resume normally anyway. This is to prevent waiting forever
	 * on display updates that may be related to spinning wheels or progress
	 * updates of sorts etc...
	 */
	private static final long TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE = 1000 * 10;// ms

	protected final UiAutomation mUiAutomation;

	public UiAutomation getUiAutomation() {
		return mUiAutomation;
	}

	private final InteractionController mInteractionController;

	private final QueryController mQueryController;

	public UiAutomatorBridge(UiAutomation uiAutomation) {
		mUiAutomation = uiAutomation;
		mInteractionController = new InteractionController(this);
		mQueryController = new QueryController(this);
		// 打开web增强
		setWebMode(false);
		// 关闭高速模式
		setFastMode(true);
	}

	InteractionController getInteractionController() {
		return mInteractionController;
	}

	QueryController getQueryController() {
		return mQueryController;
	}

	public void setOnAccessibilityEventListener(
			OnAccessibilityEventListener listener) {
		mUiAutomation.setOnAccessibilityEventListener(listener);
	}

	public AccessibilityNodeInfo getRootInActiveWindow() {
		return mUiAutomation.getRootInActiveWindow();
	}

	public boolean injectInputEvent(InputEvent event, boolean sync) {
		return mUiAutomation.injectInputEvent(event, sync);
	}

	public boolean setRotation(int rotation) {
		return mUiAutomation.setRotation(rotation);
	}

	public void setWebMode(boolean webmode) {
		AccessibilityServiceInfo info = mUiAutomation.getServiceInfo();
		if (webmode) {
			info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;

		} else {
			info.flags &= ~AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
		}
		mUiAutomation.setServiceInfo(info);
	}

	boolean fastMode;

	public void setFastMode(boolean compressed) {
		this.fastMode = compressed;
		AccessibilityServiceInfo info = mUiAutomation.getServiceInfo();
		if (compressed) {
			// 只加载重要的，高速模式
			info.flags &= ~AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
			if (UiDevice.API_LEVEL_ACTUAL >= Build.VERSION_CODES.LOLLIPOP) {
				info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
			}
		} else {
			// 加载不重要的，慢速模式
			info.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
			if (UiDevice.API_LEVEL_ACTUAL >= Build.VERSION_CODES.LOLLIPOP) {
				info.flags &= ~AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
			}

		}
		mUiAutomation.setServiceInfo(info);
	}

	public boolean getFastMode() {
		return fastMode;
	}

	public abstract int getRotation();

	public abstract boolean isScreenOn();

	public void waitForIdle() {
		waitForIdle(TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE);
	}

	public void waitForIdle(long timeout) {
		try {
			mUiAutomation.waitForIdle(QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE,
					timeout);
		} catch (TimeoutException te) {
			Log.w(LOG_TAG, "Could not detect idle state.", te);
		}
	}

	public AccessibilityEvent executeCommandAndWaitForAccessibilityEvent(
			Runnable command, AccessibilityEventFilter filter,
			long timeoutMillis) throws TimeoutException {
		return mUiAutomation.executeAndWaitForEvent(command, filter,
				timeoutMillis);
	}

	public boolean takeScreenshot(File storePath, int quality) {
		Bitmap screenshot = mUiAutomation.takeScreenshot();
		if (screenshot == null) {
			return false;
		}
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(storePath));
			if (bos != null) {
				screenshot.compress(Bitmap.CompressFormat.PNG, quality, bos);
				bos.flush();
			}
		} catch (IOException ioe) {
			Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
			return false;
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException ioe) {
					/* ignore */
				}
			}
			screenshot.recycle();
		}
		return true;
	}

	public boolean performGlobalAction(int action) {
		return mUiAutomation.performGlobalAction(action);
	}

	public abstract Display getDefaultDisplay();

	public abstract long getSystemLongPressTime();
}
