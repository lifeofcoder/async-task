package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.entity.AddAsyncTaskException;
import com.lifeofcoder.asynctask.core.entity.TaskInfo;

/**
 * 任务修改器接口
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface TaskAppender extends Typer {
    /**
     * 添加任务，如果添加失败会抛出异常：AddAsyncTaskException
     * 只有在引擎没有启动或者正在关闭的时候，向引擎中添加任务才会抛出异常。其他正常其情况不会抛出异常
     * 该情况普遍发生在：在系统关闭的过程中，还有任务向引擎中添加，此时就会抛出异常。
     * @param task 待添加的任务
     * @throws AddAsyncTaskException 抛出的异常
     */
    void addTask(TaskInfo task) throws AddAsyncTaskException;

    /**
     * 添加任务，如果添加失败会抛出异常：AddAsyncTaskException
     * 只有在引擎没有启动或者正在关闭的时候，向引擎中添加任务才会抛出异常。其他正常其情况不会抛出异常
     * 该情况普遍发生在：在系统关闭的过程中，还有任务向引擎中添加，此时就会抛出异常。
     * @param task 待添加的任务
     * @param taskExecutorListener 任务执行器回调监听
     * @throws AddAsyncTaskException 抛出的异常
     */
    void addTask(TaskInfo task, TaskExecutorListener taskExecutorListener) throws AddAsyncTaskException;
}
