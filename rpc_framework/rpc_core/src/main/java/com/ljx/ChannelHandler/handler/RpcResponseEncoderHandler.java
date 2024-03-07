package com.ljx.ChannelHandler.handler;

import com.ljx.compress.Compressor;
import com.ljx.compress.CompressorFactory;
import com.ljx.serialize.Serializer;
import com.ljx.serialize.SerializerFactory;
import com.ljx.transport.message.MessageFormatConstant;
import com.ljx.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * -----响应头
 * 4B magic(魔数) -> rpc!.getBytes()
 * 1B version(版本) -> 1
 * 2B headrLength(报文头长度)
 * 4B FullLength(报文总长度)
 * 1B 响应码(响应状态）
 * 1B SerializeType(序列化类型)
 * 1B CompressType(压缩类型)
 * 8B RequestId(请求id)
 *
 * -----body
 *
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class RpcResponseEncoderHandler extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        //魔数值编码
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //版本号编码
        byteBuf.writeByte(MessageFormatConstant.VETSION);
        //headrLength编码
        byteBuf.writeShort(MessageFormatConstant.HEAD_LENGTH);
        //FullLength编码先空着
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageFormatConstant.FULL_FIELD_LENGTH);
        //请求响应状态编码
        byteBuf.writeByte(rpcResponse.getCode());
        //序列化类型编码
        byteBuf.writeByte(rpcResponse.getSerializeType());
        //压缩类型编码
        byteBuf.writeByte(rpcResponse.getCompressType());
        //请求id编码
        byteBuf.writeLong(rpcResponse.getRequestId());
        byteBuf.writeLong(rpcResponse.getTimestamp());
        byte[] body = null;
        //响应体序列化
        if(rpcResponse.getBody()!=null){
            Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType()).getSerializer();
            body = serializer.serialize(rpcResponse.getBody());
            //响应体压缩
            Compressor compressor = CompressorFactory.getCompressor(rpcResponse.getCompressType()).getCompressor();
            body = compressor.compress(body);
        }
        if(body!=null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = ((body==null)?0:body.length);
        //重新处理报文总长度
        int writerIndex = byteBuf.writerIndex();
        //将写指针的位置移动到FullLength的位置
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEAD_FIELD_LENGTH);
        //FullLength编码
        byteBuf.writeInt(MessageFormatConstant.HEAD_LENGTH + bodyLength);
        //写指针归位
        byteBuf.writerIndex(writerIndex);
        if(log.isDebugEnabled()){
            log.debug("响应【{}】已经在服务端完成编码。",rpcResponse.getRequestId());
        }
    }
}
