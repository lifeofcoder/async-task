package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.result.TaskExecuteResult;

/**
 * 任务执行器
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface TaskExecutor<T> {
    /**
     * 执行任务
     * @param task 待执行任务
     * @param retryTimes 重试次数
     */
    TaskExecuteResult execute(Task<T> task, int retryTimes);
}
