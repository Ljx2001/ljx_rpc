package com.ljx;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.zookeeper.data.Id;

import java.util.concurrent.atomic.LongAdder;

/**
 * 请求id的生成器
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public class IdGenerator {
    //雪花算法
    //时间戳 42bit
    //机房号 5bit
    //机器号 5bit
    //序列号 12bit
    //总长度64bit

    //起始时间戳
    private static final long START_TIME_STAMP = DateUtil.get("2024-01-01").getTime();
    //机房号位数
    private static final long DATA_CENTER_BIT = 5L;
    //机器号位数
    private static final long MACHINE_BIT = 5L;
    //序列号位数
    private static final long SEQUENCE_BIT = 12L;
    //机房号最大值
    public static final long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);
    //机器号最大值
    public static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    //序列号最大值
    private static final long MAX_SEQUENCE_NUM = ~(-1L << SEQUENCE_BIT);
    //时间戳移动的位数
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BIT + DATA_CENTER_BIT + MACHINE_BIT;
    //机房号移动的位数
    private static final long DATA_CENTER_LEFT_SHIFT = SEQUENCE_BIT + MACHINE_BIT;
    //机器号移动的位数
    private static final long MACHINE_LEFT_SHIFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    //上一次的时间戳
    private long lastTimeStamp = -1L;
    public IdGenerator(long dataCenterId, long machineId) {
        if(dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0){
            throw new IllegalArgumentException("dataCenterId can't be greater than MAX_DATA_CENTER_NUM or less than 0");
        }
        if(machineId > MAX_MACHINE_NUM || machineId < 0){
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }
    public long getId(){
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_TIME_STAMP;
        //判断时钟回拨
        if(timeStamp < lastTimeStamp){
            throw new RuntimeException("时钟回拨，拒绝生成id");
        }
        //如果是同一个时间节点必须自增
        if(timeStamp == lastTimeStamp) {
            if(sequenceId.sum() > MAX_SEQUENCE_NUM){
                //如果序列号超过最大值，等待下一个时间节点
                while (timeStamp == lastTimeStamp){
                    currentTime = System.currentTimeMillis();
                    timeStamp = currentTime - START_TIME_STAMP;
                }
                sequenceId.reset();
                timeStamp = currentTime - START_TIME_STAMP;
            }
            sequenceId.increment();
        }else if(timeStamp > lastTimeStamp){
            sequenceId.reset();
        }
        lastTimeStamp = timeStamp;
        long sequence = this.sequenceId.sum();
        return timeStamp<<TIMESTAMP_LEFT_SHIFT | dataCenterId<<DATA_CENTER_LEFT_SHIFT | machineId<<MACHINE_LEFT_SHIFT | sequence;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1, 2);
        LongAdder longAdder = new LongAdder();
        for (int i = 0; i < 100; i++) {
            new Thread(()->{
                System.out.println(idGenerator.getId());
            }).start();
        }
    }
}
