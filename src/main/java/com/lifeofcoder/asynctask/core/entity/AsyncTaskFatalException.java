package com.lifeofcoder.asynctask.core.entity;

/**
 * 异步任务引擎致命异常，这类异常通常被认为无法通过重试的方式解决。
 * 因此：当任务执行期间抛出（由框架或者业务）该异常，就回到导致任务会立即终止并归档告警。并不会执行重试策略
 *
 * @author xbc
 * @date 2020/1/14
 */
public class AsyncTaskFatalException extends AsyncTaskException {
    public AsyncTaskFatalException(String message) {
        super(message);
    }

    public AsyncTaskFatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
