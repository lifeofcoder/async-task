package com.lifeofcoder.asynctask.performance.single;

import com.lifeofcoder.asynctask.performance.CallableTaskHolder;
import com.lifeofcoder.asynctask.performance.Config;
import com.lifeofcoder.asynctask.performance.TaskWorkHandler;
import com.lifeofcoder.asynctask.core.DisruptorBuilder;
import com.lifeofcoder.asynctask.performance.CallableTask;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 性能测试
 *
 * @author xbc
 * @date 2020/1/21
 */
public class SingleDisruptorPoolTest {
    List<CallableTask> callableTaskLis = new ArrayList<>();

    @Before
    public void init() {
        for (int i = 0; i < Config.SINGLE_TASK_NUMS; i++) {
            callableTaskLis.add(new CallableTask());
        }
    }

    @Test
    public void testDisruptorPool() {
        Disruptor disruptor = DisruptorBuilder.<CallableTaskHolder>newInstance()
                .setRingBufferSize(Config.ringBufferSize)
                .setEventFactory(new EventFactory<CallableTaskHolder>() {
                    @Override
                    public CallableTaskHolder newInstance() {
                        return new CallableTaskHolder();
                    }
                })
                .build();

        List<WorkHandler> taskExecutorList = new ArrayList<>(Config.nThreads);
        for (int i = 0; i < Config.nThreads; i++) {
            taskExecutorList.add(new TaskWorkHandler());
        }
        disruptor.handleEventsWithWorkerPool(taskExecutorList.toArray(new WorkHandler[]{}));

        disruptor.start();

        Long start = System.currentTimeMillis();
        List<Future<?>> futureTaskList = new ArrayList<>(Config.SINGLE_TASK_NUMS);
        for (final CallableTask task : callableTaskLis) {
            final FutureTask tempTask = new FutureTask(task);
            futureTaskList.add(tempTask);
            disruptor.publishEvent(new EventTranslator<CallableTaskHolder>() {
                @Override
                public void translateTo(CallableTaskHolder event, long sequence) {
                    event.setCallableTask(tempTask);
                }
            });
        }

        waitForEnding(futureTaskList);

        System.out.println("SingleDisruptorPool:" + (System.currentTimeMillis() - start));
    }

    private void wait4Done(Future<?> future) {
        if (future.isDone()) {
            return;
        }

        try {
            future.get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void waitForEnding(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            wait4Done(future);
        }
    }
}
