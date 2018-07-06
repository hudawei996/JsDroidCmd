package com.jsdroid.util;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ShellUtil {

	public static String exec(String cmd) {
		StringBuilder ret = new StringBuilder();
		List<String> result = null;
		result = Shell.SH.run(cmd);
		for (String line : result) {
			ret.append(line);
			ret.append("\n");
		}
		return ret.toString().trim();
	}
	public static String su(String cmd) {
		StringBuilder ret = new StringBuilder();
		List<String> result = null;
		result = Shell.SU.run(cmd);
		for (String line : result) {
			ret.append(line);
			ret.append("\n");
		}
		return ret.toString().trim();
	}
}
