package com.lifeofcoder.asynctask.performance;

import java.util.concurrent.FutureTask;

/**
 *
 *
 * @author xbc
 * @date 2020/2/13
 */
public class CallableTaskHolder {
    private FutureTask callableTask;

    public CallableTaskHolder() {
    }

    public CallableTaskHolder(FutureTask callableTask) {
        this.callableTask = callableTask;
    }

    public FutureTask getCallableTask() {
        return callableTask;
    }

    public void setCallableTask(FutureTask callableTask) {
        this.callableTask = callableTask;
    }
}
