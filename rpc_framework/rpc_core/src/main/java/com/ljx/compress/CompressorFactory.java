package com.ljx.compress;

import com.ljx.compress.impl.GzipCompressor;
import com.ljx.serialize.SerializerWrapper;
import com.ljx.serialize.impl.HessianSerializer;
import com.ljx.serialize.impl.JdkSerializer;
import com.ljx.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class CompressorFactory {
    private final static ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte,CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static{
        CompressorWrapper gzip = new CompressorWrapper((byte)1,"gzip",new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte)1,gzip);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param compressorType
     * @return
     */
    public static CompressorWrapper getCompressor(String compressorType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorWrapper == null) {
            if (log.isErrorEnabled()) {
                log.error("未找到对应的压缩器,使用默认的gzip压缩器");
            }

            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }
    public static CompressorWrapper getCompressor(byte compressorCode) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if (compressorWrapper == null) {
            if (log.isErrorEnabled()) {
                log.error("未找到对应的压缩器,使用默认的gzip压缩器");
            }

            return COMPRESSOR_CACHE_CODE.get((byte)1);
        }
        return compressorWrapper;
    }
}
