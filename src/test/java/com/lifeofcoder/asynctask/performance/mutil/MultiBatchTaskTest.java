package com.lifeofcoder.asynctask.performance.mutil;

import com.lifeofcoder.asynctask.core.AsyncTaskEngine;
import com.lifeofcoder.asynctask.performance.CallableTask;
import com.lifeofcoder.asynctask.performance.Config;
import com.lifeofcoder.asynctask.performance.MyThread;
import org.apache.http.concurrent.BasicFuture;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 性能测试
 *
 * @author xbc
 * @date 2020/1/21
 */
public class MultiBatchTaskTest {
    List<CallableTask> callableTaskLis = new ArrayList<>();

    @Before
    public void init() {
        for (int i = 0; i < Config.MULTI_TASK_NUMS; i++) {
            callableTaskLis.add(new CallableTask());
        }
    }

    @Test
    public void testBatchTask() throws InterruptedException {
        AsyncTaskEngine engine = AsyncTaskEngine.buildBatchTask(Config.nThreads, Config.ringBufferSize).build();
        engine.startEngine();
        testPerformance("MultiBatchTask", engine);
    }

    private void testPerformance(String remark, final ExecutorService executorService) {
        try {
            Long start = System.currentTimeMillis();
            List<Future<?>> resultList = new ArrayList<>();
            for (int i = 0; i < Config.PRODUCERS; i++) {
                BasicFuture<String> future = new BasicFuture<>(null);
                resultList.add(future);
                MyThread thread = new MyThread(future, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            executorService.invokeAll(callableTaskLis);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
