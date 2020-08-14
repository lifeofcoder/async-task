package com.lifeofcoder.asynctask.core.register;

import com.lifeofcoder.asynctask.core.entity.TaskExecutorHolder;

/**
 * 任务执行器注册中心接口
 *
 * @author xbc
 * @date 2020/1/14
 */
public abstract class TaskExecutorRegister implements TyperRegister<TaskExecutorHolder> {
    @Override
    public void register(TaskExecutorHolder taskExecutorHolder) {
        register(taskExecutorHolder.getType(), taskExecutorHolder);
    }
}
