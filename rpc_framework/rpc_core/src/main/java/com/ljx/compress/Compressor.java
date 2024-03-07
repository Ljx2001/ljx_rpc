package com.ljx.compress;

/**
 * 抽象的压缩器
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public interface Compressor {
    /**
     * 压缩
     * @param bytes 待压缩的字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压
     * @param bytes 待解压的字节数组
     * @return 解压后的字节数组
     */
    byte[] decompress(byte[] bytes);
}
