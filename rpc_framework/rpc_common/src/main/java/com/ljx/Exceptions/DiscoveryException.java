package com.ljx.Exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
@Slf4j
public class DiscoveryException extends RuntimeException{

    public DiscoveryException(String notSupportedRegistryType) {
        log.error(notSupportedRegistryType);
    }
    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
