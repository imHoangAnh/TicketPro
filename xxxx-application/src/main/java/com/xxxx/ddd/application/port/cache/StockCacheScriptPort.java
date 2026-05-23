package com.xxxx.ddd.application.port.cache;

public interface StockCacheScriptPort {

    int decreaseStock(String key, int quantity);

    boolean increaseStock(String key, int quantity);
}
