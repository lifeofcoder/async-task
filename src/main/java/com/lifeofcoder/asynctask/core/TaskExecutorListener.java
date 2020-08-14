package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.result.TaskListenerResult;

/**
 * 任务执行监听
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface TaskExecutorListener extends Typer {
    /**
     * 执行成功回调[不要抛出异常，抛出异常会导致该任务直接终止，并归档告警]
     * @param taskInfo 任务信息
     */
    void onSuccess(Task taskInfo);

    /**
     * 执行失败回调。抛出AsyncTaskException异常会导致业务直接终止。其他异常则会继续重试任务。
     * @param taskInfo 任务信息
     * @param  throwable 异常信息，如果不存在则为null
     * @return 如果返回false，则需要执行引擎告警。返回true，说明业务自己处理了错误，所以执行器不会做任何告警等异常处理。
     */
    TaskListenerResult onFail(Task taskInfo, Throwable throwable);

    /**
     * 任务执行完成后的回调：包括告警，归档等业务执行完成。onSuccess或者onFail都在改方法之前执行
     * @param taskInfo 任务信息
     */
    void onComplete(Task taskInfo);
}
