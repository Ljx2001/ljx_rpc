package com.ljx.ChannelHandler.handler;

import com.ljx.compress.Compressor;
import com.ljx.compress.CompressorFactory;
import com.ljx.serialize.Serializer;
import com.ljx.serialize.SerializerFactory;
import com.ljx.transport.message.MessageFormatConstant;
import com.ljx.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class RpcResponseDecoderHandler extends LengthFieldBasedFrameDecoder {
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //将ctx中的响应对象转换为ByteBuf
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf){
        RpcResponse rpcResponse = RpcResponse.builder().build();
        //1.解析魔数值
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //校验魔数是否匹配
        for(int i = 0;i<magic.length;i++){
            if (magic[i]!=MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("获得的请求不合法！");
            }
        }
        //2.解析版本号
        byte version = byteBuf.readByte();
        if(version>MessageFormatConstant.VETSION){
            throw new RuntimeException("获得的请求版本不被支持！");
        }
        //3.解析报文头长度
        short headLength = byteBuf.readShort();
        //4.解析报文总长度
        int fullLength = byteBuf.readInt();
        //5.解析响应码
        byte responseCode = byteBuf.readByte();
        rpcResponse.setCode(responseCode);
        //6.解析序列化类型
        byte serializeType = byteBuf.readByte();
        rpcResponse.setSerializeType(serializeType);
        //7.解析压缩类型
        byte compressType = byteBuf.readByte();
        rpcResponse.setCompressType(compressType);
        //8.解析请求id
        long requestId = byteBuf.readLong();
        rpcResponse.setRequestId(requestId);
        //9.解析时间戳
        long timestamp = byteBuf.readLong();
        rpcResponse.setTimestamp(timestamp);
        //如果是心跳请求，直接返回
//        if(responseCode == RequestType.HEART_BEAT.getId()){
//            return rpcResponse;
//        }
        //9.解析请求体
        int bodyLength = fullLength - headLength;
        byte[] payload = new byte[bodyLength];
        byteBuf.readBytes(payload);
        Object body = null;
        if(payload!=null&&payload.length>0){
            //解压缩
            Compressor compressor = CompressorFactory.getCompressor(compressType).getImpl();
            payload = compressor.decompress(payload);
            //反序列化
            Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
            body = serializer.deserialize(payload, Object.class);
        }
        rpcResponse.setBody(body);
        if(log.isDebugEnabled()){
            log.debug("响应【{}】已经在调用端完成解码。",rpcResponse.getRequestId());
        }
        return rpcResponse;
    }

    public RpcResponseDecoderHandler() {
        super(
                //最大的帧长度
                MessageFormatConstant.MAX_FRAME_LENGTH,
                //长度字段的偏移量
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEAD_FIELD_LENGTH,
                //长度字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                //todo 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEAD_FIELD_LENGTH +MessageFormatConstant.FULL_FIELD_LENGTH),
                //跳过的字段
                0);
    }

}