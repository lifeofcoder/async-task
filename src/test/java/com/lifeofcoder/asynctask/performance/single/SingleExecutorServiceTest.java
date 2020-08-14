package com.lifeofcoder.asynctask.performance.single;

import com.lifeofcoder.asynctask.core.NamedThreadFactory;
import com.lifeofcoder.asynctask.performance.CallableTask;
import com.lifeofcoder.asynctask.performance.Config;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 性能测试
 *
 * @author xbc
 * @date 2020/1/21
 */
public class SingleExecutorServiceTest {
    List<CallableTask> callableTaskLis = new ArrayList<>();

    @Before
    public void init() {
        for (int i = 0; i < Config.SINGLE_TASK_NUMS; i++) {
            callableTaskLis.add(new CallableTask());
        }
    }

    @Test
    public void testExecutorService() throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(Config.nThreads, Config.nThreads, 0L, TimeUnit.MILLISECONDS,
                                                                 new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("inter-executor-", false));
        testPerformance("SingleExecutorService", executorService);
    }

    private void testPerformance(String remark, ExecutorService executorService) {
        try {
            Long start = System.currentTimeMillis();
            List<Future<String>> futures = executorService.invokeAll(callableTaskLis);
            System.out.println(remark + ":" + (System.currentTimeMillis() - start));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
