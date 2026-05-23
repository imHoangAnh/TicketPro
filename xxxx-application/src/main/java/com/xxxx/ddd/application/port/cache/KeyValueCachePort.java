package com.xxxx.ddd.application.port.cache;

public interface KeyValueCachePort {

    void setString(String key, String value);

    String getString(String key);

    void setObject(String key, Object value);

    <T> T getObject(String key, Class<T> targetClass);

    void delete(String key);

    void setInt(String key, int value);

    int getInt(String key);
}
