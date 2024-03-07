package com.ljx.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.ljx.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object==null){
            return null;
        }
        byte[] bytes = JSON.toJSONBytes(object);
        if(log.isDebugEnabled()){
            log.debug("使用json序列化对象【{}】成功",object);
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes==null||clazz==null||bytes.length==0){
            return null;
        }
        T object = JSON.parseObject(bytes, clazz);
        if(log.isDebugEnabled()){
            log.debug("使用json反序列化类【{}】成功",object);
        }
        return object;
    }
}
