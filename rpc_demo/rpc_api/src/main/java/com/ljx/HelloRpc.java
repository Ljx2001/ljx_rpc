package com.ljx;

import com.ljx.annotation.TryTimes;

/**
 * @Author: liujinxing
 * @Date: 2024-03-01
 */
public interface HelloRpc {
    /**
     * 通用接口，server和client都需要依赖
     * @param msg，发送的具体消息
     * @return
     */
    @TryTimes(tryTimes = 4, interval = 1000)
    String sayHi(String msg);
}
