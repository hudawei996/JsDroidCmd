package com.jsdroid.core;

import org.mozilla.javascript.Context;

import com.jsdroid.config.IConfigService;
import com.jsdroid.proxy.ProxyServiceManager;

public class Config {
	private static Config instance = new Config();

	public static Config getInstance() {

		return instance;
	}

	private Config() {

	}

	public Object read(Object key) {
		try {
			IConfigService config = ProxyServiceManager.getService(
					JsGlobal.getGlobal().pkg + ".config", IConfigService.class);
			if (config != null) {
				Object ret = config.read(Context.toString(key));
				if (ret != null) {
					return ret.toString();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void save(Object key, Object value) {
		try {
			IConfigService config = ProxyServiceManager.getService(
					JsGlobal.getGlobal().pkg + ".config", IConfigService.class);
			if (config != null) {
				config.save(Context.toString(key), Context.toString(value));
			}
		} catch (Exception e) {
		}
	}
}
