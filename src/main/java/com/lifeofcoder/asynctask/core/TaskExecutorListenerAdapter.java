package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.result.TaskListenerResult;

/**
 * TaskExecutorListener适配器
 *
 * @author xbc
 * @date 2020/1/17
 */
public abstract class TaskExecutorListenerAdapter implements TaskExecutorListener {
    @Override
    public void onSuccess(Task taskInfo) {
    }

    @Override
    public TaskListenerResult onFail(Task taskInfo, Throwable throwable) {
        return TaskListenerResult.success();
    }

    @Override
    public void onComplete(Task taskInfo) {
    }
}
