package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.entity.*;
import com.lifeofcoder.asynctask.core.impl.DefaultTyper;
import com.lifeofcoder.asynctask.core.register.TaskExecutorRegister;
import com.lifeofcoder.asynctask.core.result.TaskExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务组合执行器(组合模式)
 *
 * @author xbc
 * @date 2020/1/13
 */
public class TaskExecutorComposite<T> extends DefaultTyper implements TaskExecutor<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorComposite.class);

    private TaskExecutorRegister taskExecutorRegistry;
    private TaskExecuteCallback callback;

    public TaskExecutorComposite(TaskExecutorRegister taskExecutorRegistry, TaskExecuteCallback callback) {
        this.taskExecutorRegistry = taskExecutorRegistry;
        this.callback = callback;
    }

    @Override
    public TaskExecuteResult execute(Task<T> task, int retryTimes) {
        try {
            return execute0((TaskInfoInner) task);
        }
        catch (Exception e) {
            LOGGER.error("Failed to execute task[" + task + "].", e);
            return TaskExecuteResult.exception(e);
        }
    }

    private TaskExecuteResult execute0(TaskInfoInner task) {
        TaskInfoInner taskInfoInner = task;
        TaskExecutorHolder taskExecutorHolder = taskExecutorRegistry.get(taskInfoInner.getType());
        if (null == taskExecutorHolder) {
            String systemError = Messager.format(Messager.NO_TASK_EXECUTOR, taskInfoInner.getType());
            TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(taskInfoInner)
                    .setSysErrorMsg(systemError).build();
            onFail(taskInfoInner, taskExecutedInfo);
            return TaskExecuteResult.fail();
        }

        TaskExecuteResult taskExecuteResult = null;
        int tempRetryTimes = 0;
        int maxRetryTimes = taskExecutorHolder.getTaskConfig().getMaxRetryTimes();
        Exception throwable = null;
        do {
            try {
                if (tempRetryTimes > 0) {
                    LOGGER.info("Try to execute task[" + taskInfoInner + "] again.");
                }

                taskExecuteResult = doExecute(taskInfoInner, tempRetryTimes);
                //succeeded
                if (TaskExecuteResult.isSuccess(taskExecuteResult)) {
                    callback.onSuccess(taskInfoInner);
                    return TaskExecuteResult.success();
                }

                //普通业务异常，即业务自己自己执行失败，尝试重试
                //retry
                LOGGER.info("Failed to execute task[" + taskInfoInner + "],"
                            + " because of a business error[" + TaskExecuteResult.getMessage(taskExecuteResult) + "].");

            }
            //系统异常，该类异常一般无法修复，所以无需重试。直接归档并告警
            catch (AsyncTaskFatalException | Error error) {
                LOGGER.error("Failed to execute task because of a fatal exception[" + error + "]");
                TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(taskInfoInner)
                        .setSysErrorMsg(Messager.SYSTEM_EXCEPTION)
                        .setException(error).build();
                onFail(taskInfoInner, taskExecutedInfo);
                return TaskExecuteResult.fail();
            }
            //普通异常，直接重试
            catch (Exception exception) {
                //retry and log
                LOGGER.info("Failed to execute task[" + taskInfoInner + "],"
                            + " because a business exception[" + exception + "] causes its executing error!");
                throwable = exception;
            }
        } while (++tempRetryTimes <= maxRetryTimes);

        String systemError = Messager.format(Messager.REACHED_MAX_TIMES, maxRetryTimes);
        String bizError = TaskExecuteResult.getMessage(taskExecuteResult, Messager.EXECUTE_EXCEPTION);
        TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(taskInfoInner)
                .setSysErrorMsg(systemError)
                .setBizErrorMsg(bizError)
                .setException(throwable).build();
        onFail(taskInfoInner, taskExecutedInfo);
        return TaskExecuteResult.fail();
    }

    private void onFail(TaskInfoInner taskInfoInner, TaskExecutedInfo taskExecutedInfo) {
        callback.onFail(taskInfoInner, taskExecutedInfo);
    }

    private TaskExecuteResult doExecute(Task taskInfo, int retryTimes) {
        if (null == taskInfo) {
            //never happen
            throw new AsyncTaskException("待执行任务为null");
        }

        TaskExecutorHolder taskExecutorHolder = taskExecutorRegistry.get(taskInfo.getType());
        if (null == taskExecutorHolder) {
            throw new AsyncTaskException("类型为[" + taskInfo.getType() + "]的任务没有可用执行类.");
        }

        TaskExecuteResult taskExecuteResult = taskExecutorHolder.getTaskExecutor().execute(taskInfo, retryTimes);
        if (null == taskExecuteResult) {
            //never happed unless business implement error
            throw new AsyncTaskException("执行器[" + taskExecutorHolder.getTaskExecutor().getClass().getSimpleName() + "]返回null.");
        }

        return taskExecuteResult;
    }

    interface TaskExecuteCallback {
        void onFail(TaskInfoInner taskInfoInner, TaskExecutedInfo taskExecutedInfo);
        void onSuccess(TaskInfoInner taskInfo);
    }
}
