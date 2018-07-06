package com.jsdroid.core;

import java.util.ArrayList;
import java.util.List;

import com.jsdroid.event.JsDroidEvent;

public class JsEvent extends Thread {
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

	private JsEvent() {
		eventListners = new ArrayList<JsEventListner>();
		start();
	}

	List<JsEventListner> eventListners;
	boolean start;
	String errMessage;

	@Override
	public void run() {
		super.run();
		start = true;
		int event[] = new int[7];
		while (start) {
			try {
				JsDroidEvent.readEvent(event);
				for (JsEventListner listner : eventListners) {
					listner.onEvent(event);
				}
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				
			}
		}
	}

	public void addEventListener(JsEventListner eventListener) {
		eventListners.add(eventListener);
	}

	public void removeListener(JsEventListner eventListner) {
		eventListners.remove(eventListner);
	}

	private static JsEvent instance = new JsEvent();

	public static JsEvent getInstance() {
		return instance;
	}
}
