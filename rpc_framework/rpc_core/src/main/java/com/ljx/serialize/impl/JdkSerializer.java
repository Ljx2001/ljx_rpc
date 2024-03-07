package com.ljx.serialize.impl;

import com.ljx.serialize.Serializer;
import com.ljx.Exceptions.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object==null){
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream))
        {
            objectOutputStream.writeObject(object);
            if(log.isDebugEnabled()){
                log.debug("序列化对象【{}】成功",object);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化对象【{}】时出现异常",object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes==null||clazz==null){
            return null;
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream))
        {
            Object object = objectInputStream.readObject();
            if(log.isDebugEnabled()){
                log.debug("反序列化类【{}】成功",object);
            }
            return (T) object;
        } catch (ClassNotFoundException|IOException e) {
            log.error("反序列化对象【{}】时出现异常",clazz);
            throw new SerializeException(e);
        }
    }
}
