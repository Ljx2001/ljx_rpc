package com.ljx.compress;

import com.ljx.compress.impl.GzipCompressor;
import com.ljx.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class CompressorFactory {
    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte,ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static{
        ObjectWrapper gzip = new ObjectWrapper<>((byte)1,"gzip",new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte)1,gzip);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param compressorType
     * @return
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> objectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (objectWrapper == null) {
            if (log.isErrorEnabled()) {
                log.error("未找到对应的压缩器,使用默认的gzip压缩器");
            }

            return COMPRESSOR_CACHE.get("gzip");
        }
        return objectWrapper;
    }
    public static ObjectWrapper<Compressor> getCompressor(byte compressorCode) {
        ObjectWrapper<Compressor> objectWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if (objectWrapper == null) {
            if (log.isErrorEnabled()) {
                log.error("未找到对应的压缩器,使用默认的gzip压缩器");
            }

            return COMPRESSOR_CACHE_CODE.get((byte)1);
        }
        return objectWrapper;
    }

    /**
     * 添加一个新的压缩策略
     * @param objectWrapper
     */
    public static void addCompressor(ObjectWrapper<Compressor> objectWrapper) {
        COMPRESSOR_CACHE.put(objectWrapper.getName(), objectWrapper);
        COMPRESSOR_CACHE_CODE.put(objectWrapper.getCode(), objectWrapper);
    }
}
