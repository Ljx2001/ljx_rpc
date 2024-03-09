package com.ljx.serialize;


import com.ljx.config.ObjectWrapper;
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
    private final static ConcurrentHashMap<String,ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte,ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static{
        ObjectWrapper<Serializer> hessian = new ObjectWrapper<Serializer>((byte)1,"hessian",new HessianSerializer());
        ObjectWrapper<Serializer> jdk = new ObjectWrapper<Serializer>((byte)2,"jdk",new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<Serializer>((byte)3,"json",new JsonSerializer());
        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);
        SERIALIZER_CACHE_CODE.put((byte)1,hessian);
        SERIALIZER_CACHE_CODE.put((byte)2, jdk);
        SERIALIZER_CACHE_CODE.put((byte)3, json);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializerType
     * @return
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializerType) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializerType);
        if(serializerWrapper == null){
            if(log.isErrorEnabled()){
                log.error("未找到对应的序列化器,使用默认的jdk序列化器");
            }
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }
    public static ObjectWrapper<Serializer> getSerializer(byte serializerCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE_CODE.get(serializerCode);
        if(serializerWrapper == null){
            if(log.isErrorEnabled()){
                log.error("未找到对应的序列化器,使用默认的jdk序列化器");
            }
            return SERIALIZER_CACHE_CODE.get((byte)1);
        }
        return serializerWrapper;
    }

    /**
     * 添加一个新的序列化策略
     * @param serializerType 序列化的类型
     * @param code 序列化的编码
     * @param serializer 具体的实现
     */
    public static void addSerializer(ObjectWrapper<Serializer> serializerWrapper) {
        SERIALIZER_CACHE.put(serializerWrapper.getName(),serializerWrapper);
        SERIALIZER_CACHE_CODE.put(serializerWrapper.getCode(),serializerWrapper);
    }
}
