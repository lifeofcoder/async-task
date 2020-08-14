package com.lifeofcoder.asynctask.core.impl;

import com.lifeofcoder.asynctask.core.entity.AsyncTaskException;
import com.lifeofcoder.asynctask.core.entity.TaskExecutorHolder;
import com.lifeofcoder.asynctask.core.register.TaskExecutorRegister;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务处理器注册中心实现类
 *
 * @author xbc
 * @date 2020/1/13
 */
public class TaskExecutorRegisterImpl extends TaskExecutorRegister {
    //TaskType -> TasHandler
    private Map<String, TaskExecutorHolder> taskHandlerMap = new HashMap<>();

    public void register(String type, TaskExecutorHolder taskExecutorHolder) {
        validate(type, taskExecutorHolder);
        taskHandlerMap.put(type, taskExecutorHolder);
    }

    private void validate(String type, TaskExecutorHolder taskExecutorHolder) {
        if (null == type || taskExecutorHolder == null) {
            throw new AsyncTaskException("Type and taskExecutorHolder are both required.");
        }

        if (taskExecutorHolder.getTaskConfig() == null || taskExecutorHolder.getTaskExecutor() == null) {
            throw new AsyncTaskException("TaskConfig and TaskExecutor can't be null.");
        }

        if (taskHandlerMap.containsKey(type)) {
            String existHandlerName = taskHandlerMap.get(type).getClass().getSimpleName();
            String newHandlerName = taskExecutorHolder.getClass().getSimpleName();
            throw new AsyncTaskException("类型为[" + type + "]的TaskExecutor存在多个实现类[" + existHandlerName + ", " + newHandlerName + "].");
        }
    }

    public TaskExecutorHolder get(String type) {
        return taskHandlerMap.get(type);
    }

    @Override
    public boolean exists(String type) {
        return taskHandlerMap.containsKey(type);
    }
}