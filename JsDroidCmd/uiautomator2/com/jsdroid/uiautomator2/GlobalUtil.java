package com.jsdroid.uiautomator2;

import java.lang.reflect.Method;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;

public class GlobalUtil {

	public static void launch(String pkg) {
		Context context = ActivityThread.currentActivityThread()
				.getSystemContext();
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(
				pkg);
		startActivity(intent);
	}

	public static void startActivity(Intent intent) {
		IActivityManager am = ActivityManagerNative.getDefault();

		Method[] methods = am.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals("startActivity")) {
				if (methods[i].getParameterTypes().length == 10) {
					try {
						methods[i]
								.invoke(am,
										null,
										"android",
										intent,
										null,
										null,
										null,
										0,
										Intent.FLAG_ACTIVITY_NEW_TASK
												| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
										null, null);
						break;
					} catch (Exception e) {
					}
				} else if (methods[i].getParameterTypes().length == 13) {
					try {
						methods[i].invoke(am, null, intent, null, null, 0,
								null, null, 0, true, false, null, null, null);
						break;
					} catch (Exception e) {
					}
				}
			}
		}
	}

}
