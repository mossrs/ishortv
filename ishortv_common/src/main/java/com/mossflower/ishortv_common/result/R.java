package com.mossflower.ishortv_common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @author z's'b
 */
@Data
public class R<T> implements Serializable, CommonMsgCode {
    private String msg;
    private Integer code;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setMsg(MSG_OK);
        r.setCode(CODE_OK);
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setMsg(MSG_OK);
        r.setCode(CODE_OK);
        r.setData(null);
        return r;
    }

    public static <T> R<T> err() {
        R<T> r = new R<>();
        r.setMsg(MSG_ERR);
        r.setCode(CODE_ERR);
        r.setData(null);
        return r;
    }

    public static <T> R<T> err(String msg) {
        R<T> r = new R<>();
        r.setMsg(msg);
        r.setCode(CODE_ERR);
        r.setData(null);
        return r;
    }

    public static <T> R<T> err(T data) {
        R<T> r = new R<>();
        r.setMsg(MSG_ERR);
        r.setCode(CODE_ERR);
        r.setData(data);
        return r;
    }

}
