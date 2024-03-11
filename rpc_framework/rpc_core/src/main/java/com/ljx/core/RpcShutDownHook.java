package com.ljx.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author LiuJixing
 * @Date 10/3/2024
 */
public class RpcShutDownHook extends Thread{
    @Override
    public void run() {
        //打开挡板
        ShutDownHolder.BLOCKER.set(true);
        //等待计数器归零
        long start = System.currentTimeMillis();
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(ShutDownHolder.REQUEST_COUNTER.sum() == 0L||System.currentTimeMillis()-start>10000L){
                break;
            }
        }
        //阻塞结束后，放行，执行其他操作
    }
}
