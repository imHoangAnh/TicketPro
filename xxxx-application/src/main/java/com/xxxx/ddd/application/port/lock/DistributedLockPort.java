package com.xxxx.ddd.application.port.lock;

public interface DistributedLockPort {

    DistributedLock getDistributedLock(String lockKey);
}
