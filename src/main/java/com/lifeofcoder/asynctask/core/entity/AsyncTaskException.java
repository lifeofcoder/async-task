package com.lifeofcoder.asynctask.core.entity;

/**
 * 异步任务引擎内部异常
 *
 * @author xbc
 * @date 2020/1/13
 */
public class AsyncTaskException extends  RuntimeException {
    public AsyncTaskException(String message) {
        super(message);
    }

    public AsyncTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
