package com.lifeofcoder.asynctask.core.result;

/**
 * 任务处理器执行结果
 *
 * @author xbc
 * @date 2020/1/13
 */
public class TaskExecuteResult extends ExceptionMessageResult {
    public TaskExecuteResult(boolean success, String errorMsg, Exception exception) {
        super(success, errorMsg, exception);
    }

    public static final TaskExecuteResult success() {
        return new TaskExecuteResult(true, null, null);
    }

    public static final TaskExecuteResult success(String message) {
        return new TaskExecuteResult(true, message, null);
    }

    public static final TaskExecuteResult fail() {
        return new TaskExecuteResult(false, null, null);
    }

    public static TaskExecuteResult fail(String errorMsg) {
        return new TaskExecuteResult(false, errorMsg, null);
    }

    public static TaskExecuteResult exception(Exception e) {
        return exception(e, "");
    }

    public static TaskExecuteResult exception(Exception e, String message) {
        return new TaskExecuteResult(false, message, e);
    }
}