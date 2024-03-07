package com.ljx.serialize;


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
public class SerializerFactory {
    private final static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte,SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static{
        SerializerWrapper jdk = new SerializerWrapper((byte)1,"jdk",new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte)2,"json",new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte)3,"hessian",new HessianSerializer());
        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);
        SERIALIZER_CACHE_CODE.put((byte)1,jdk);
        SERIALIZER_CACHE_CODE.put((byte)2, json);
        SERIALIZER_CACHE_CODE.put((byte)3, hessian);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializerType
     * @return
     */
    public static SerializerWrapper getSerializer(String serializerType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializerType);
        if(serializerWrapper == null){
            if(log.isErrorEnabled()){
                log.error("未找到对应的序列化器,使用默认的jdk序列化器");
            }
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }
    public static SerializerWrapper getSerializer(byte serializerCode) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_CODE.get(serializerCode);
        if(serializerWrapper == null){
            if(log.isErrorEnabled()){
                log.error("未找到对应的序列化器,使用默认的jdk序列化器");
            }
            return SERIALIZER_CACHE_CODE.get((byte)1);
        }
        return serializerWrapper;
    }
}
