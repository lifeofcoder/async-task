package com.lifeofcoder.asynctask.core.entity;

import com.lifeofcoder.asynctask.core.Task;
import com.lifeofcoder.asynctask.core.TaskExecutorListener;

/**
 * 内部使用的任务对象，是一个适配器
 *
 * @author xbc
 * @date 2020/1/14
 */
public class TaskInfoInner<T> implements Task<T> {
    private static final long serialVersionUID = 4547192576985077907L;

    private Task<T> delegate;
    private TaskExecutorListener taskExecutorListener;

    public TaskInfoInner() {
    }

    public TaskInfoInner(Task<T> delegate, TaskExecutorListener taskExecutorListener) {
        this.delegate = delegate;
        this.taskExecutorListener = taskExecutorListener;
    }

    public void setTaskInfo(Task<T> task) {
        delegate = task;
    }

    public <R> R getDelegateTask() {
        return (R) delegate;
    }

    @Override
    public T getData() {
        return delegate.getData();
    }

    @Override
    public String getBusinessCode() {
        return delegate.getBusinessCode();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    public TaskExecutorListener getTaskExecutorListener() {
        return taskExecutorListener;
    }

    public void setTaskExecutorListener(TaskExecutorListener taskExecutorListener) {
        this.taskExecutorListener = taskExecutorListener;
    }

    public void from(TaskInfo taskInfo, TaskExecutorListener taskExecutorListener) {
        resest();
        setTaskInfo(taskInfo);
        setTaskExecutorListener(taskExecutorListener);
    }

    public void resest() {
        setTaskInfo(null);
        setTaskExecutorListener(null);
    }

    @Override
    public String toString() {
        return "TaskInfo{" + "type='" + getType() + '\'' + ", businessCode='" + getBusinessCode() + '\'' + '}';
    }
}
