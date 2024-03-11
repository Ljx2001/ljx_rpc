package com.ljx.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author LiuJixing
 * @Date 10/3/2024
 */
public class ShutDownHolder {
    public static AtomicBoolean BLOCKER = new AtomicBoolean(false);
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
