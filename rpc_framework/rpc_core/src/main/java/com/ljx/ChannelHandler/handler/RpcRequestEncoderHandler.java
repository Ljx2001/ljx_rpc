package com.ljx.ChannelHandler.handler;

import com.ljx.compress.Compressor;
import com.ljx.compress.CompressorFactory;
import com.ljx.enumeration.RequestType;
import com.ljx.serialize.Serializer;
import com.ljx.serialize.SerializerFactory;
import com.ljx.RpcBootstrap;
import com.ljx.transport.message.MessageFormatConstant;
import com.ljx.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * -----请求头
 * 4B magic(魔数) -> rpc!.getBytes()
 * 1B version(版本) -> 1
 * 2B headrLength(报文头长度)
 * 4B FullLength(报文总长度)
 * 1B RequestType(请求类型)
 * 1B SerializeType(序列化类型)
 * 1B CompressType(压缩类型)
 * 8B RequestId(请求id)
 *
 * -----body
 *
 * 出栈时第一个处理器，用于将RpcRequest编码为ByteBuf
 * @Author LiuJixing
 * @Date 5/3/2024
 */
@Slf4j
public class RpcRequestEncoderHandler extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        //魔数值编码
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //版本号编码
        byteBuf.writeByte(MessageFormatConstant.VETSION);
        //headrLength编码
        byteBuf.writeShort(MessageFormatConstant.HEAD_LENGTH);
        //FullLength编码先空着
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageFormatConstant.FULL_FIELD_LENGTH);
        //请求类型编码
        byteBuf.writeByte(rpcRequest.getRequestType());
        //序列化类型编码
        byteBuf.writeByte(rpcRequest.getSerializeType());
        //压缩类型编码
        byteBuf.writeByte(rpcRequest.getCompressType());
        //请求id编码
        byteBuf.writeLong(rpcRequest.getRequestId());
        byteBuf.writeLong(rpcRequest.getTimestamp());
        //请求体编码
        byte[] body =null;
        if (rpcRequest.getRequestPayload()!=null) {
            //根据配置的序列化方式进行序列化
            Serializer serializer = SerializerFactory.getSerializer(rpcRequest.getSerializeType()).getSerializer();
            body = serializer.serialize(rpcRequest.getRequestPayload());
            //根据配置的压缩方式进行压缩
            Compressor compressor = CompressorFactory.getCompressor(rpcRequest.getCompressType()).getCompressor();
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
        if(log.isDebugEnabled()&&body != null){
            log.debug("请求【{}】已经完成了报文的编码。",rpcRequest.getRequestId());
        }
    }
}
