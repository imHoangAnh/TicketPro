package com.xxxx.ddd.infrastructure.distributed.redisson;

import com.xxxx.ddd.application.port.lock.DistributedLockPort;

public interface RedisDistributedService extends DistributedLockPort {

    @Override
    RedisDistributedLocker getDistributedLock(String lockKey);
}
