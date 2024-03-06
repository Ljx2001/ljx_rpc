package com.ljx.transport.message;

/**
 * @Author LiuJixing
 * @Date 5/3/2024
 */
public class MessageFormatConstant {
    public static final byte[] MAGIC = "rpc!".getBytes();
    public static final byte VETSION = 1;
    public static final int VERSION_LENGTH = 1;
    public static final int HEAD_FIELD_LENGTH = 2;
    public static final short HEAD_LENGTH = (byte)(MAGIC.length+ 1 + 2 + 4 + 1 + 1 + 1 + 8);
    public static final int MAX_FRAME_LENGTH = 4 * 1024 * 1024;

    public static final int FULL_FIELD_LENGTH = 4;
}
