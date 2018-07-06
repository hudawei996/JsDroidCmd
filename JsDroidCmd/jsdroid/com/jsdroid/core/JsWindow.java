package com.jsdroid.core;

import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

public class JsWindow {

	public JsWindow(Context context) {
		mContext = context;
		mTN = new TN();
		mTN.mY = context.getResources().getDimensionPixelSize(
				com.android.internal.R.dimen.toast_y_offset);
		mTN.mGravity = context.getResources().getInteger(
				com.android.internal.R.integer.config_toastDefaultGravity);
	}

	public void show() {
		// INotificationManager service = getService();
		// String pkg = mContext.getOpPackageName();
		TN tn = mTN;
		TextView textView = new TextView(mContext);
		System.out.println("show");
		textView.setText("hello jsdroid!");
		textView.setBackgroundColor(Color.RED);
		textView.setTextSize(20);
		textView.setTextColor(Color.WHITE);
		tn.mNextView = textView;
		System.out.println("show start");
		tn.show();
		System.out.println("show end");
	}

	private static INotificationManager sService;

	static private INotificationManager getService() {
		if (sService != null) {
			return sService;
		}
		sService = INotificationManager.Stub.asInterface(ServiceManager
				.getService("notification"));
		return sService;
	}

	private static class TN {
		final Runnable mShow = new Runnable() {
			@Override
			public void run() {
				handleShow();
			}
		};

		final Runnable mHide = new Runnable() {
			@Override
			public void run() {
				handleHide();
				// Don't do this in handleHide() because it is also invoked by
				// handleShow()
				mNextView = null;
			}
		};

		private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
		final Handler mHandler = new Handler();

		int mGravity;
		int mX, mY;
		float mHorizontalMargin;
		float mVerticalMargin;

		View mView;
		View mNextView;

		WindowManager mWM;

		TN() {
			// XXX This should be changed to use a Dialog, with a Theme.Toast
			// defined that sets up the layout params appropriately.
			final WindowManager.LayoutParams params = mParams;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.width = WindowManager.LayoutParams.WRAP_CONTENT;
			params.format = PixelFormat.TRANSLUCENT;
			params.windowAnimations = com.android.internal.R.style.Animation_Toast;
			params.type = WindowManager.LayoutParams.TYPE_TOAST;
			params.setTitle("Toast");
			params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}

		public void show() {
			handleShow();
		}

		public void hide() {
			handleHide();
		}

		public void handleShow() {
			if (mView != mNextView) {
				// remove the old view if necessary
				mView = mNextView;
				Context context = mView.getContext().getApplicationContext();
				String packageName = mView.getContext().getOpPackageName();
				System.out.println("pkg:" + packageName);
				if (context == null) {
					context = mView.getContext();
				}
				mWM = (WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE);
				// We can resolve the Gravity here by using the Locale for
				// getting
				// the layout direction
				final Configuration config = mView.getContext().getResources()
						.getConfiguration();
				final int gravity = Gravity.getAbsoluteGravity(mGravity,
						config.getLayoutDirection());
				mParams.gravity = gravity;
				if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
					mParams.horizontalWeight = 1.0f;
				}
				if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
					mParams.verticalWeight = 1.0f;
				}
				mParams.x = mX;
				mParams.y = mY;
				mParams.verticalMargin = mVerticalMargin;
				mParams.horizontalMargin = mHorizontalMargin;
				mParams.packageName = packageName;
				if (mView.getParent() != null) {
					mWM.removeView(mView);
				}
				System.out.println("add view start");
				try {
					mWM.addView(mView, mParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("add view end");
				trySendAccessibilityEvent();
			}
		}

		private void trySendAccessibilityEvent() {
			AccessibilityManager accessibilityManager = AccessibilityManager
					.getInstance(mView.getContext());
			if (!accessibilityManager.isEnabled()) {
				return;
			}
			// treat toasts as notifications since they are used to
			// announce a transient piece of information to the user
			AccessibilityEvent event = AccessibilityEvent
					.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
			event.setClassName(getClass().getName());
			event.setPackageName(mView.getContext().getPackageName());
			mView.dispatchPopulateAccessibilityEvent(event);
			accessibilityManager.sendAccessibilityEvent(event);
		}

		public void handleHide() {
			if (mView != null) {
				// note: checking parent() just to make sure the view has
				// been added... i have seen cases where we get here when
				// the view isn't yet added, so let's try not to crash.
				if (mView.getParent() != null) {
					mWM.removeView(mView);
				}
				mView = null;
			}
		}

	}

	private Context mContext;
	private TN mTN;

}
