package com.ljx.protection;

/**
 * 基于令牌桶算法的限流器
 * @Author LiuJixing
 * @Date 9/3/2024
 */
public class TokenBuketRateLimiter implements RateLimiter{
    //令牌的数量，>0 说明有令牌，能放行，放行-1，否则拒绝
    private int tokens;
    //桶的容量
    private final int capacity;
    //速率：每秒加多少个令牌
    private final int rate;
    //上一次加令牌的时间
    private long lastTokenTime;
    public TokenBuketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        this.tokens = capacity;
        this.lastTokenTime = System.currentTimeMillis();
    }

    /**
     * 判断请求是否放行
     * @return true 放行，false 拒绝
     */
    public synchronized boolean allowRequest() {
        //1.给令牌桶添加令牌
        //计算从现在到上一次的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        Long timeInterval = currentTime - lastTokenTime;
        //添加令牌
        if(timeInterval >= 1000/rate) {
            int addTokens = (int) (timeInterval * rate / 1000);
            tokens = Math.min(capacity, tokens + addTokens);
            this.lastTokenTime = currentTime;
        }
        //2.自己获取令牌
        if(tokens > 0) {
            tokens--;
            System.out.println("放行");
            return true;
        } else {
            System.out.println("拒绝");
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBuketRateLimiter rateLimiter = new TokenBuketRateLimiter(10, 10);
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rateLimiter.allowRequest();
        }
    }
}
