package com.lifeofcoder.asynctask.core.entity;

import com.lifeofcoder.asynctask.core.TaskExecutor;
import com.lifeofcoder.asynctask.core.Typer;

/**
 * 任务执行器的持有者
 *
 * @author xbc
 * @date 2020/1/14
 */
public class TaskExecutorHolder implements Typer {
    /**
     * 任务类型和配置数据
     */
    private TaskConfig taskConfig;
    private TaskExecutor taskExecutor;

    public TaskExecutorHolder(TaskConfig taskConfig, TaskExecutor taskExecutor) {
        this.taskConfig = taskConfig;
        this.taskExecutor = taskExecutor;
    }

    public TaskConfig getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(TaskConfig taskConfig) {
        this.taskConfig = taskConfig;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public static TaskExecutorHolder build(String type, int maxRetryTimes, TaskExecutor taskExecutor) {
        TaskConfig taskConfig = new TaskConfig(type, maxRetryTimes);
        return new TaskExecutorHolder(taskConfig, taskExecutor);
    }

    @Override
    public String getType() {
        return taskConfig.getType();
    }
}
