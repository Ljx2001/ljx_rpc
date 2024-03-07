package com.ljx.compress.impl;

import com.ljx.compress.Compressor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if(log.isDebugEnabled()){
                log.debug("使用gzip对字节数组进行完成压缩");
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompletionException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(bais)
        ) {
            byte[] result = gzipInputStream.readAllBytes();
            if(log.isDebugEnabled()){
                log.debug("使用gzip对字节数组进行完成解压");
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行解压时发生异常",e);
            throw new CompletionException(e);
        }
    }
}
