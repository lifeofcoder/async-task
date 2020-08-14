package com.lifeofcoder.asynctask.performance.mutil;

import com.lifeofcoder.asynctask.core.DisruptorBuilder;
import com.lifeofcoder.asynctask.performance.*;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.http.concurrent.BasicFuture;
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
public class MultiDisruptorPoolTest {
    List<CallableTask> callableTaskLis = new ArrayList<>();

    @Before
    public void init() {
        for (int i = 0; i < Config.MULTI_TASK_NUMS; i++) {
            callableTaskLis.add(new CallableTask());
        }
    }

    @Test
    public void testDisruptorPool() {
        Disruptor disruptor = DisruptorBuilder.<CallableTaskHolder>newInstance().setRingBufferSize(Config.ringBufferSize)
                .setEventFactory(new EventFactory<CallableTaskHolder>() {
                    @Override
                    public CallableTaskHolder newInstance() {
                        return new CallableTaskHolder();
                    }
                })
                .build();

        List<WorkHandler> taskExecutorList = new ArrayList<>();
        for (int i = 0; i < Config.nThreads; i++) {
            taskExecutorList.add(new TaskWorkHandler());
        }
        disruptor.handleEventsWithWorkerPool(taskExecutorList.toArray(new WorkHandler[]{}));

        disruptor.start();

        testPerformance("MultiDisruptorPool", disruptor);
    }

    private void testPerformance(String remark, final Disruptor disruptor) {
        try {
            Long start = System.currentTimeMillis();
            List<Future<?>> resultList = new ArrayList<>();
            for (int i = 0; i < Config.PRODUCERS; i++) {
                BasicFuture<String> future = new BasicFuture<>(null);
                resultList.add(future);
                MyThread thread = new MyThread(future, new Runnable() {
                    @Override
                    public void run() {
                        List<Future<?>> futureTaskList = new ArrayList<>();
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
                    }
                });
                thread.start();

            }
            waitForEnding(resultList);
            System.out.println(remark + ":" + (System.currentTimeMillis() - start));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
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
