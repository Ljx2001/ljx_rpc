package com.ljx.protection;

/**
 * 限流器的接口
 * @Author LiuJixing
 * @Date 9/3/2024
 */
public interface RateLimiter {
    //判断是否允许新的请求进入 true 允许，false 拒绝
    boolean allowRequest();
}
