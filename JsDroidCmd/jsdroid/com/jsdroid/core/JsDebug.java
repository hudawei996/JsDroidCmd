package com.jsdroid.core;

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

public class JsDebug implements Debugger {
	private int lineNumber;
	private String sourceName;

	public int getLineNumber() {
		return lineNumber;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getLineString() {
		return "【" + sourceName + "#" + lineNumber + "】";
	}

	DebugFrame debugFrame = new DebugFrame() {

		@Override
		public void onExit(Context cx, boolean byThrow, Object resultOrException) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onExceptionThrown(Context cx, Throwable ex) {

		}

		@Override
		public void onEnter(Context cx, String sourceName,
				Scriptable activation, Scriptable thisObj, Object[] args) {

		}

		@Override
		public void onDebuggerStatement(Context cx) {

		}

		@Override
		public void onLineChange(Context cx, String sourceName, int lineNumber) {
			try {
				JsDebug.this.sourceName = sourceName;
				JsDebug.this.sourceName = new File(sourceName).getName();
			} catch (Exception e) {
			}
			JsDebug.this.lineNumber = lineNumber;
		}
	};

	@Override
	public void handleCompilationDone(Context cx, DebuggableScript fnOrScript,
			String source) {

	}

	@Override
	public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {

		return debugFrame;
	}

	private JsDebug() {
	};

	private static JsDebug instance = new JsDebug();

	public static JsDebug getInstance() {
		return instance;
	}
}
