package com.jsdroid.record;

import android.graphics.Point;

import com.jsdroid.core.JsEvent;
import com.jsdroid.core.JsEventListner;
import com.jsdroid.uiautomator2.UiDevice;

public class Recorder implements JsEventListner {
	public interface RecordListener {
		public void onRecord(JsRecord record);
	}

	// TYPE
	static final int EV_SYN = 0x00; // 刷新xy坐标
	static final int EV_KEY = 0x01;
	static final int EV_ABS = 0x03;
	// CODE
	static final int BTN_TOUCH = 0x14a;// key
	static final int ABS_MT_POSITION_X = 0x35;// abs
	static final int ABS_MT_POSITION_Y = 0x36;// abs
	static final int SYN_REPORT = 0X00;
	// VALUE
	static final int TOUCH_UP = 0;// key
	static final int TOUCH_DOWN = 1;// key

	private static Recorder INSTANCE = new Recorder();

	public static Recorder getInstance() {
		return INSTANCE;
	}

	// 截图大小
	int imageWidth;
	int imageHeight;
	// 屏幕大小
	int screenWidth;
	int screenHeight;

	Thread eventThread;
	JsRecord lastRecord;
	RecordListener recordListener;
	Point lastPoint = new Point();
	RecordThread lastRecordThread;

	private Recorder() {

	}

	public void init() {
		screenWidth = UiDevice.getInstance().getDisplayUnRotationWidth();
		screenHeight = UiDevice.getInstance().getDisplayUnRotationHeight();
		float scale = 1;
		if (screenWidth < 480 || screenHeight < 480) {
			scale = 1;
		} else if (screenWidth < screenHeight) {
			scale = (float) (480.0 / screenWidth);
		} else {
			scale = (float) (480.0 / screenHeight);
		}
		imageWidth = (int) (screenWidth * scale);
		imageHeight = (int) (screenHeight * scale);
	}

	private void createRecord(int[] event) {
		// 结束上次的录制
		endRecord();
		lastRecord = new JsRecord();
		lastRecord.xmin = event[3];
		lastRecord.xmax = event[4];
		lastRecord.ymin = event[5];
		lastRecord.ymax = event[6];
		lastRecord.time = System.currentTimeMillis();
		lastRecord.imageWidth = imageWidth;
		lastRecord.imageHeight = imageHeight;
		lastRecordThread = new RecordThread(lastRecord, recordListener);
		lastRecordThread.start();

	}

	private void endRecord() {
		if (lastRecordThread != null) {
			lastRecordThread.endAndSend();
		}
		lastRecord = null;
		lastRecordThread = null;
	}

	private void recordEvent(int event[]) {
		if (event[0] == EV_KEY && event[1] == BTN_TOUCH) {
			if (event[2] == TOUCH_DOWN) {
				// 按下事件
				// 创建新录制
				createRecord(event);
			} else if (event[2] == TOUCH_UP) {
				// 如果是抬起事件
				// 结束上次的录制
				endRecord();
			}
		} else if (event[0] == EV_ABS) {
			// 如果是滑动事件:将坐标添加到JsRecord;
			if (event[1] == ABS_MT_POSITION_X) {
				lastPoint.x = event[2];
			} else if (event[1] == ABS_MT_POSITION_Y) {
				lastPoint.y = event[2];
			}
		} else if (event[0] == EV_SYN && event[1] == SYN_REPORT) {
			// 记录点
			if (lastRecord != null) {
				System.out.println("move:" + lastPoint);
				lastRecord.points.add(new Point(lastPoint));
			}
		}
	}

	public void setRecordListener(RecordListener recordListener) {
		this.recordListener = recordListener;
	}

	public void start() {
		endRecord();
		JsEvent.getInstance().addEventListener(this);
	}

	public void quit() {
		JsEvent.getInstance().removeListener(this);
		endRecord();
	}

	@Override
	public void onEvent(int[] event) {
		recordEvent(event);
	}
}
