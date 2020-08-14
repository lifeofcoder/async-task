package com.lifeofcoder.asynctask.performance.single;

import com.lifeofcoder.asynctask.core.AsyncTaskEngine;
import com.lifeofcoder.asynctask.performance.Config;
import com.lifeofcoder.asynctask.performance.CallableTask;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * 性能测试
 *
 * @author xbc
 * @date 2020/1/21
 */
public class SingleBatchTaskTest {
    List<CallableTask> callableTaskLis = new ArrayList<>();
    @Before
    public void init() {
        for (int i = 0; i < Config.SINGLE_TASK_NUMS; i++) {
            callableTaskLis.add(new CallableTask());
        }
    }

    @Test
    public void testBatchTask() throws InterruptedException {
        AsyncTaskEngine engine = AsyncTaskEngine.buildBatchTask(Config.nThreads, Config.ringBufferSize).build();
        engine.startEngine();
        testPerformance("SingleBatchTask", engine);
    }

    private void testPerformance(String remark, ExecutorService executorService) {
        try {
            Long start = System.currentTimeMillis();
            List<Future<String>> futures = executorService.invokeAll(callableTaskLis);
//            waitForEndingString(futures);
            System.out.println(remark + ":" + (System.currentTimeMillis() - start));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
