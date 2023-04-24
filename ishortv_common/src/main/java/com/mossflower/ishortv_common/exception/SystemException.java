package com.mossflower.ishortv_common.exception;

/**
 * @author z's'b
 * @date 2023/2/21 星期二 20:43
 * @description
 */
public class SystemException extends RuntimeException {

    public SystemException(Exception e) {
        super(e);
    }

    public SystemException(String message) {
        super(message);
    }
}
