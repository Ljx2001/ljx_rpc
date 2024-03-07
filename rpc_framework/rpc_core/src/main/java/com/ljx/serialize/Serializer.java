package com.ljx.serialize;

/**
 * 抽象的序列化器
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public interface Serializer {
    /**
     * 抽象的用来做序列化的方法
     * @param object 待序列化的对象实例
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化的方法
     * @param bytes 待反序列化的字节数组
     * @param clazz 目标类的class对象
     * @return 目标实例
     * @param <T> 目标类泛型
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
