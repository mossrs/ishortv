package com.mossflower.ishortv_common.exception;

import com.mossflower.ishortv_common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintStream;
import java.rmi.ServerException;

/**
 * @author z's'b
 * 异常处理器 将所有的错误都抛到表现层 然后可以精准捕获每一个异常
 * <p>
 * 第一个是用于返回自己捕获的异常，在业务层打印错误日志并交由处理器返回用户友好提示
 * 第二个是用于返回未捕获的异常，由处理器统一打印日志并返回用户友好提示
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 用来处理用户操作失误的异常
     *
     * @return R
     */
    @ExceptionHandler(ClientException.class)
    public R<String> catchClientException(ClientException e) {
        log.error("客户端异常:{}", printEx(e));
        return R.err(e.getMessage());
    }

    /**
     * 用来处理系统内部错误的可捕获的异常
     *
     * @return R
     */
    @ExceptionHandler(SystemException.class)
    public R<String> catchSystemException(SystemException e) {
        log.error("服务端异常:{}", printEx(e));
        return R.err(e.getMessage());
    }

    /**
     * 用来处理系统内部错误的不可捕获的异常
     * <p>
     * 可能是服务端的错误，也可能是客户端的错误
     */
    @ExceptionHandler(Exception.class)
    public R<Object> catchSqlException(Exception e) {
        log.error("未知异常:{}", printEx(e));
        return R.err("系统异常，请勿重复操作!");
    }

    private String printEx(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        e.printStackTrace(printStream);
        String exception = baos.toString();
        try {
            printStream.close();
            baos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return exception;
    }
}
