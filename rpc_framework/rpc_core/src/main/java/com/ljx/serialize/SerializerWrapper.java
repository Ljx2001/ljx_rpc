package com.ljx.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
