package com.mossflower.ishortv_common.result;

/**
 * @author z's'b
 */
public interface CommonMsgCode {
    /**
     * 设置一些通用的消息码
     * */
    String MSG_OK = "操作成功";
    String MSG_ERR = "操作失败";
    Integer CODE_OK = 1;
    Integer CODE_ERR = 0;

    /**
     * 对外提供消息
     * @return 消息
     */
    String getMsg();

    /**
     * 对外提供状态码
     * @return 状态码
     */
    Integer getCode();
}
