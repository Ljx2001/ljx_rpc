package com.ljx.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器
 * @Author LiuJixing
 * @Date 9/3/2024
 */
public class CircuitBreaker {
    private volatile Boolean isOpen = false;
    //总的请求次数
    private AtomicInteger requestCount = new AtomicInteger(0);
    //异常的请求数
    private AtomicInteger errorCount = new AtomicInteger(0);
    //允许最大的异常数
    private final int maxErrorCount;
    //允许的最大错误率
    private final float maxErrorRate;

    public CircuitBreaker(int maxErrorCount, float maxErrorRate) {
        this.maxErrorCount = maxErrorCount;
        this.maxErrorRate = maxErrorRate;
    }
    /**
     * 判断是否开启熔断器
     */
    public boolean isBreak() {
        if(isOpen) {
            return true;
        } else {
            //判断错误数
            if(errorCount.get() > maxErrorCount) {
                this.isOpen = true;
                return true;
            }
            //判断错误率
            if(errorCount.get() > 0 && requestCount.get() > 0 && (float) errorCount.get() / requestCount.get() > maxErrorRate) {
                this.isOpen = true;
                return true;
            }
            return false;
        }
    }

    /**
     * 每次发生请求或异常应该进行记录
     */
    public void record(boolean isSuccess) {
        if(!isSuccess) {
            errorCount.incrementAndGet();
        } else {
            requestCount.incrementAndGet();
        }
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorCount.set(0);
    }

    public static void main(String[] args) throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 1.1F);
        Random random = new Random();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                circuitBreaker.reset();
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                circuitBreaker.record(true);
                int num = random.nextInt(100);
                if(num < 70) {
                    circuitBreaker.record(false);
                }
                Boolean aBreak = circuitBreaker.isBreak();
                System.out.println(aBreak?"熔断":"正常");
            }
        }).start();
        Thread.sleep(100000000);
    }
}
