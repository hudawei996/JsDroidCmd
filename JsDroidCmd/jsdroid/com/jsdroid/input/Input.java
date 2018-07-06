package com.jsdroid.input;

import java.util.ArrayList;
import java.util.List;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.inputmethod.InputMethodInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.internal.view.IInputMethodManager;
import com.jsdroid.util.LocalSocketUtil;

public class Input {
	static String id = "com.jsdroid/.input.CmdInputService";

	public static boolean open() {
		IInputMethodManager mImm;
		mImm = IInputMethodManager.Stub.asInterface(ServiceManager
				.getService("input_method"));
		try {
			List<String> inputList = list();
			for (int i = 0; i < inputList.size(); i++) {
				String _id = inputList.get(i);
				if (_id.contains("CmdInputService")) {
					id = _id;
					break;
				}
			}
			mImm.setInputMethodEnabled(id, true);
			mImm.setInputMethod(null, id);
			return true;
		} catch (RemoteException e) {
		}
		return false;
	}

	public static boolean close() {
		IInputMethodManager mImm;
		mImm = IInputMethodManager.Stub.asInterface(ServiceManager
				.getService("input_method"));
		try {
			mImm.setInputMethodEnabled(id, false);
			for (InputMethodInfo info : mImm.getEnabledInputMethodList()) {
				mImm.setInputMethodEnabled(info.getId(), true);
				mImm.setInputMethod(null, info.getId());
				return true;
			}
		} catch (RemoteException e) {
		}
		return false;
	}

	public static List<String> list() {
		List<String> ret = new ArrayList<String>();
		IInputMethodManager mImm;
		mImm = IInputMethodManager.Stub.asInterface(ServiceManager
				.getService("input_method"));
		try {

			for (InputMethodInfo info : mImm.getInputMethodList()) {
				ret.add(info.getId());
			}
		} catch (RemoteException e) {
		}
		return ret;
	}

	public static boolean input(String text) {
		LocalSocketUtil socketUtil = null;
		try {
			socketUtil = new LocalSocketUtil("jsdroid_input");
			JSONObject json = new JSONObject();
			json.put("type", "input");
			json.put("text", text);
			socketUtil.sendLine(json.toJSONString());
			String line = socketUtil.readLine();
			json = JSON.parseObject(line);
			return json.getBoolean("ret");
		} catch (Exception e) {
		} finally {
			try {
				socketUtil.close();
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static boolean clear() {
		return clear(1000, 1000);
	}

	public static boolean clear(int before, int after) {
		LocalSocketUtil socketUtil = null;
		try {
			socketUtil = new LocalSocketUtil("jsdroid_input");
			JSONObject json = new JSONObject();
			json.put("type", "clear");
			json.put("before", before);
			json.put("after", after);
			socketUtil.sendLine(json.toJSONString());
			String line = socketUtil.readLine();
			json = JSON.parseObject(line);
			return json.getBoolean("ret");
		} catch (Exception e) {
		} finally {
			try {
				socketUtil.close();
			} catch (Exception e) {
			}
		}
		return false;
	}
}
