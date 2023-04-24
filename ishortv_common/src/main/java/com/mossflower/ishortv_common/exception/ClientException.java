package com.mossflower.ishortv_common.exception;

/**
 * @author z's'b
 * @date 2023/2/21 星期二 20:24
 * @description 客户端异常
 */
public class ClientException extends RuntimeException {

    public ClientException(Exception e) {
        super(e);
    }

    public ClientException(String message) {
        super(message);
    }
}
