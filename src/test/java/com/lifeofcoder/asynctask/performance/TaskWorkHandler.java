package com.lifeofcoder.asynctask.performance;

import com.lmax.disruptor.WorkHandler;

/**
 *
 *
 * @author xbc
 * @date 2020/2/13
 */
public class TaskWorkHandler implements WorkHandler<CallableTaskHolder> {
    @Override
    public void onEvent(CallableTaskHolder event) throws Exception {
        event.getCallableTask().run();
    }
}
