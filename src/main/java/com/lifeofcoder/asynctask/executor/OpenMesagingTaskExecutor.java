package com.lifeofcoder.asynctask.executor;

import com.lifeofcoder.asynctask.core.Task;
import com.lifeofcoder.asynctask.core.TaskExecutor;
import com.lifeofcoder.asynctask.core.entity.TaskInfo;
import com.lifeofcoder.asynctask.core.result.TaskExecuteResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;

/**
 * OpenMessaging任务执行器
 *
 * @author xbc
 * @date 2020/1/17
 */
public class OpenMesagingTaskExecutor implements TaskExecutor<Message> {
    public static final String TYPE = "MqTask";
    private Producer producer;

    public OpenMesagingTaskExecutor(Producer producer) {
        this.producer = producer;
    }

    @Override
    public TaskExecuteResult execute(Task<Message> task, int retryTimes) {
        try {
            producer.send(task.getData());
            return TaskExecuteResult.success();
        }
        catch (Exception e) {
            return TaskExecuteResult.exception(e, "消息发送失败.");
        }
    }

    /**
     * 新建任务信息，默认的BusinessCode=messageId
     */
    public static TaskInfo newTask(Message message) {
        return newTask(message, message.header().getMessageId());
    }

    private static TaskInfo newTask(Message message, String businessCode) {
        return new TaskInfo(TYPE, businessCode, message);
    }
}
