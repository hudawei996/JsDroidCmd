package com.jsdroid.transaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.view.MotionEvent;
import android.view.View;


public class Transaction {
	Map<String, Action> actions = new LinkedHashMap<String, Action>();
	public void addAction(String stepName,Action action){
		actions.put(stepName, action);
	}
	public Object doWork() {
		String step = null;
		for (Entry<String, Action> entry : actions.entrySet()) {
			Action action = entry.getValue();
			switch (action.type()) {
			case Action.TYPE_NODE:
				Object node = action.node();
				if (node != null) {
					action.doWork(node);
					step = entry.getKey();
				}
				break;

			case Action.TYPE_PIC:
				Object point = action.findpic();
				if (point != null) {
					action.doWork(point);
				}
				break;
			}
		}
		return step;
	}
	
	
	
	public static void main(String[] args) {
		View v = null;
		
	}
}
