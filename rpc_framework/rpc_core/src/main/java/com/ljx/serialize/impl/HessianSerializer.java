package com.ljx.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.ljx.serialize.Serializer;
import com.ljx.Exceptions.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ) {
            Hessian2Output output = new Hessian2Output(baos);
            output.writeObject(object);
            output.flush();
            if(log.isDebugEnabled()){
                log.debug("使用hessian序列化对象【{}】成功",object);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("使用hessian序列化对象【{}】时出现异常",object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || bytes.length == 0||clazz == null){
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)){
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            T t = (T) hessian2Input.readObject();
            if(log.isDebugEnabled()){
                log.debug("使用hessian反序列化类【{}】成功",clazz);
            }
            return t;
        } catch (IOException e) {
            log.error("使用hessian反序列化类【{}】时出现异常",clazz);
            throw new SerializeException(e);
        }
    }
}
