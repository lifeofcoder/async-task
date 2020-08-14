package com.lifeofcoder.asynctask.core;

import org.junit.Test;

/**
 * 批量任务测试
 *
 * @author xbc
 * @date 2020/1/23
 */
public class BatchTaskEngineTest {
    /**
     * 批量任务
     */
    @Test
    public void test4Batch() {
        AsyncTaskEngine engine = AsyncTaskEngine.buildBatchTask(5, 16).build();
        engine.startEngine();
//        engine.submit(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("Bocai");
//                throw new RuntimeException("Exception Test.");
//            }
//        });

        engine.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Bocai2");
                throw new RuntimeException("Exception Test222.");
            }
        });
    }
}
