package com.lifeofcoder.asynctask.core.result;

/**
 * 业务回调执行结果
 * 回调执行结果，如果执行失败，则会触发异步任务引擎引擎的告警功能。
 *
 * @author xbc
 * @date 2020/1/13
 */
public class TaskListenerResult extends ExceptionMessageResult {
    public TaskListenerResult(boolean success, String errorMsg, Exception exception) {
        super(success, errorMsg, exception);
    }

    public static final TaskListenerResult success() {
        return new TaskListenerResult(true, null, null);
    }

    public static final TaskListenerResult success(String message) {
        return new TaskListenerResult(true, message, null);
    }

    public static final TaskListenerResult fail() {
        return new TaskListenerResult(false, null, null);
    }

    public static TaskListenerResult fail(String errorMsg) {
        return new TaskListenerResult(false, errorMsg, null);
    }

    public static TaskListenerResult exception(Exception e) {
        return exception(e, "");
    }

    public static TaskListenerResult exception(Exception e, String message) {
        return new TaskListenerResult(false, message, e);
    }
}
