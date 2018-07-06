package com.jsdroid.transaction;

public interface Action {
	public static final int TYPE_NODE = 0;
	public static final int TYPE_PIC = 1;
	//类型
	public int type();
	public Object node();
	public Object findpic();
	public Object doWork(Object data);
}
