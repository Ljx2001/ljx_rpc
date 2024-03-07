package com.ljx.compress;

import com.ljx.serialize.Serializer;
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
public class CompressorWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
