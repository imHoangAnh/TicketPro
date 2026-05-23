package com.xxxx.ddd.application.service.order;

public class OrderAppException extends RuntimeException {

    private final String code;

    public OrderAppException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
