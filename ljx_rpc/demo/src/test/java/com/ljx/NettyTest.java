package com.ljx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {
    @Test
    public void testByteBuf() {
        ByteBuf buf = Unpooled.buffer(10);
        for (int i = 0; i < 10; i++) {
            buf.writeByte(i);
        }
        for (int i = 0; i < buf.capacity(); i++) {
            System.out.println(buf.getByte(i));
        }
    }
    @Test
    public void CompositeByteBuf() {
        ByteBuf header = Unpooled.buffer(10);
        ByteBuf body = Unpooled.buffer(10);
        CompositeByteBuf httpbuf = Unpooled.compositeBuffer();
        httpbuf.addComponents(header, body);
    }
    @Test
    public void testWrapper(){
        byte[] buf = new byte[1024];
        byte[] buf1 = new byte[1024];
        //将两个数组包装成一个ByteBuf,Bytebuf和原数组共享同一块内存，不产生复制
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf1);
    }
    @Test
    public void testSlice(){
        ByteBuf buf = Unpooled.buffer(10);
        for (int i = 0; i < 10; i++) {
            buf.writeByte(i);
        }
        //Slice方法返回的是原ByteBuf的一个切片，共享同一块内存
        ByteBuf slice = buf.slice(0, 5);
        for (int i = 0; i < slice.capacity(); i++) {
            System.out.println(slice.getByte(i));
        }
    }
    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        //魔数，用于辨识该协议
        message.writeBytes("ljx".getBytes(StandardCharsets.UTF_8));
        //协议版本号
        message.writeByte(1);
        //
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
        //用对象流转化为字节数组
        AppClient appClient = new AppClient();
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(appClient);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);
        System.out.println(message);
        printAsBinary(message);
    }
    public static void printAsBinary(ByteBuf byteBuf) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < byteBuf.capacity(); i++) {
            byte b = byteBuf.getByte(i);
            stringBuilder.append(Integer.toBinaryString(b));
        }
        System.out.println(stringBuilder.toString());
    }
    @Test
    public void testCompress() throws IOException {
        byte[] buf = new byte[]{12, 23, 34, 45, 56, 67, 78, 89, 90, 100, 110, 120,1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);

        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = baos.toByteArray();
        System.out.println(Arrays.toString(bytes));
    }
    @Test
    public void testDeCompress() throws IOException {
        byte[] buf = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, 17, 87, -46, -75, 112, -10, -117, -116, 74, -55, -85, 96, 100, 98, 102, 97, 101, 99, -25, -32, -28, 2, 0, -61, 69, 116, 74, 22, 0, 0, 0};
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(Arrays.toString(bytes));
    }
}
