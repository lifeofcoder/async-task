package com.lifeofcoder.asynctask.core.entity;

/**
 * 不支持的异常
 * @author xbc
 * @date 2020/1/23
 */
public class NotSupportedException extends AsyncTaskException {
    public NotSupportedException() {
        super("The operation is not supported.");
    }
}
