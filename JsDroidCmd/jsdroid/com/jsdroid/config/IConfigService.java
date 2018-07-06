package com.jsdroid.config;

public interface IConfigService {
    Object read(String key);

    void save(String key, String value);
}
