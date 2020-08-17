package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.entity.AsyncTaskException;
import com.lifeofcoder.asynctask.core.entity.TaskExecutorHolder;
import com.lifeofcoder.asynctask.core.entity.TaskInfo;
import com.lifeofcoder.asynctask.core.entity.TaskStoreInfo;
import com.lifeofcoder.asynctask.core.impl.DefaultTyper;
import com.lifeofcoder.asynctask.core.result.StoreResult;
import com.lifeofcoder.asynctask.core.result.TaskExecuteResult;
import com.lifeofcoder.asynctask.core.result.TaskListenerResult;
import com.lifeofcoder.asynctask.entity.Person;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class AsyncTaskEngineTest {
    public static final int TYPE_UNSET = -1;
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_FAIL = 2;
    public static final int TYPE_EXCEPTION = 3;

    private static final String TASK_TYPE = "task_type";

    private static final String BIZ_CODE = "bizCode";

    /**
     * ringBufferSize错误
     */
    @Test(expected = AsyncTaskException.class)
    public void test4Validate0() {
        AsyncTaskEngine engine = createEngine(0, 3, null, null, null, null);
        engine.startEngine();
    }

    /**
     * ringBufferSize错误,不是2的次方
     */
    @Test(expected = AsyncTaskException.class)
    public void test4Validate1() {
        AsyncTaskEngine engine = createEngine(14, 3, null, null, null, null);
        engine.startEngine();
    }

    /**
     * maxRetryTimes错误
     */
    @Test(expected = AsyncTaskException.class)
    public void test4Validate2() {
        AsyncTaskEngine engine = createEngine(16, 0, null, null, null, null);
        engine.startEngine();
    }

    /**
     * alarmer没有设置
     */
    @Test(expected = AsyncTaskException.class)
    public void test4Validate3() {
        AsyncTaskEngine engine = createEngine(16, 3, null, null, null, null);
        engine.startEngine();
    }

    /**
     * taskStorer都没有设置
     */
    @Test(expected = AsyncTaskException.class)
    public void test4Validate4() {
        AsyncTaskEngine engine = createEngine(16, 3, new CustomizedAlarmer(), null, null, null);
        engine.startEngine();
    }

    /**
     * 默认设置
     */
    @Test
    public void test4Validate5() {
        AsyncTaskEngine engine = AsyncTaskEngine.buildEdgeTask(16).build();
        engine.startEngine();
    }

    /**
     * 基础设置
     */
    @Test
    public void test4Validate6() {
        AsyncTaskEngine engine = createEngine(16, 3, new CustomizedAlarmer(), null, new CustomizedStorer(), null);
        engine.startEngine();
    }

    /**
     * 任务正常执行
     */
    @Test
    public void test4ExecuteSuccess() {
        CustomizedAlarmer customizedAlarmer = new CustomizedAlarmer();
        CustomizedStorer customizedStorer = new CustomizedStorer();
        AsyncTaskEngine engine = createEngine(16, 3, customizedAlarmer, null, customizedStorer, null);
        final TestTaskExecutor taskExecutor = new TestTaskExecutor(TYPE_SUCCESS);
        register(engine, taskExecutor);
        TaskAppender taskAppender = engine.startEngine();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        taskAppender.addTask(new TaskInfo(TASK_TYPE, BIZ_CODE), new CountDownTaskExecutorListener(countDownLatch));
        await(countDownLatch);
        Assert.assertEquals(TYPE_SUCCESS, taskExecutor.result());
        Assert.assertEquals(TYPE_UNSET, customizedAlarmer.result());
        Assert.assertEquals(TYPE_UNSET, customizedStorer.result());
    }

    /**
     * 任务执行失败
     */
    @Test
    public void test4ExecuteFailed() {
        CustomizedAlarmer customizedAlarmer = new CustomizedAlarmer();
        CustomizedStorer customizedStorer = new CustomizedStorer();
        AsyncTaskEngine engine = createEngine(16, 3, customizedAlarmer, null, customizedStorer, null);
        final TestTaskExecutor taskExecutor = new TestTaskExecutor(TYPE_FAIL);
        register(engine, taskExecutor);
        TaskAppender taskAppender = engine.startEngine();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        taskAppender.addTask(new TaskInfo(TASK_TYPE, BIZ_CODE), new CountDownTaskExecutorListener(countDownLatch));
        await(countDownLatch);
        Assert.assertEquals(TYPE_FAIL, taskExecutor.result());
        Assert.assertEquals(TYPE_SUCCESS, customizedStorer.result());
        Assert.assertEquals(TYPE_SUCCESS, customizedAlarmer.result());
    }

    /**
     * 任务执行异常
     */
    @Test
    public void test4ExecuteException() {
        CustomizedAlarmer customizedAlarmer = new CustomizedAlarmer();
        CustomizedStorer customizedStorer = new CustomizedStorer();
        AsyncTaskEngine engine = createEngine(16, 3, customizedAlarmer, null, customizedStorer, null);
        final TestTaskExecutor taskExecutor = new TestTaskExecutor(TYPE_EXCEPTION);
        register(engine, taskExecutor);
        TaskAppender taskAppender = engine.startEngine();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        taskAppender.addTask(new TaskInfo(TASK_TYPE, BIZ_CODE), new CountDownTaskExecutorListener(countDownLatch));
        await(countDownLatch);
        Assert.assertEquals(TYPE_EXCEPTION, taskExecutor.result());
        Assert.assertEquals(TYPE_SUCCESS, customizedStorer.result());
        Assert.assertEquals(TYPE_SUCCESS, customizedAlarmer.result());
    }

    private void await(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CountDownTaskExecutorListener extends TaskExecutorListenerAdapter {
        private CountDownLatch countDownLatch;
        public CountDownTaskExecutorListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public TaskListenerResult onFail(Task taskInfo, Throwable throwable) {
            return TaskListenerResult.fail();
        }

        @Override
        public void onComplete(Task taskInfo) {
            countDownLatch.countDown();
        }
        @Override
        public String getType() {
            return "Normal";
        }
    }

    private void register(AsyncTaskEngine engine, TaskExecutor executor) {
        TaskExecutorHolder executorHolder = TaskExecutorHolder.build(TASK_TYPE, 3, executor);
        engine.register(executorHolder);
    }

    private AsyncTaskEngine createEngine(int ringBufferSize, int maxRetryTimes, Alarmer alarmer, Alarmer defAlarmer, TaskStorer taskStorer, TaskStorer defTaskStorer) {
        return AsyncTaskEngine.buildEdgeTask(ringBufferSize)
                .setMaxRetryTimes(maxRetryTimes)
                .setAlarmer(alarmer)
                .setDefaultAlarmer(defAlarmer)
                .setTaskStorer(taskStorer)
                .setDefaultTaskMetaStorer(defTaskStorer)
                .build();
    }

    private static class TestTaskExecutor implements TaskExecutor<Person> {
        public static String ERROR_MSG = "TestTaskExecutorFailed";
        public static String EXCEPTION_MSG = "TestTaskExecutorEXCEPTON";
        private int type;
        private int result = TYPE_UNSET;
        public TestTaskExecutor(int type) {
            this.type = type;
        }

        @Override
        public TaskExecuteResult execute(Task<Person> task, int retryTimes) {
            System.out.println("[TestTaskExecutor] execute " + task);
            switch (type) {
                case TYPE_SUCCESS:
                    result = TYPE_SUCCESS;
                    return TaskExecuteResult.success();
                case TYPE_FAIL:
                    result = TYPE_FAIL;
                    return TaskExecuteResult.fail(ERROR_MSG);
                case  TYPE_EXCEPTION:
                    result = TYPE_EXCEPTION;
                    return TaskExecuteResult.exception(new ExecuteException());
            }

            throw new Error();
        }

        public int result() {
            return result;
        }
    }

    private static class ExecuteException extends RuntimeException {
    }


    private static class CustomizedAlarmer extends DefaultTyper implements Alarmer {
        public static String ERROR_MSG = "CustomizedAlarmerFailed";
        public static String EXCEPTION_MSG = "CustomizedAlarmerEXCEPTON";

        private int type;
        private int result = TYPE_UNSET;

        public CustomizedAlarmer() {
            this(TYPE_SUCCESS);
        }

        public CustomizedAlarmer(int type) {
            this.type = type;
        }

        @Override
        public boolean alarm(String message) {
            System.err.println("【CustomizedAlarmer】alarm");
            switch (type) {
                case TYPE_SUCCESS:
                    result = TYPE_SUCCESS;
                    return true;
                case TYPE_FAIL:
                    result = TYPE_FAIL;
                    return false;
                case  TYPE_EXCEPTION:
                    result = TYPE_EXCEPTION;
                    throw new AlarmerException();
            }
            throw new Error();
        }

        public int result() {
            return  result;
        }
    }

    private static class AlarmerException extends RuntimeException {
    }

    private static class CustomizedStorer extends DefaultTyper implements TaskStorer {
        public static String STORE_KEY = "CustomizedStorerStoreKey";
        public static String ERROR_MSG = "CustomizedStorerFailed";
        public static String EXCEPTION_MSG = "CustomizedStorerEXCEPTON";

        private int result = TYPE_UNSET;
        private int type;

        public CustomizedStorer() {
            this(TYPE_SUCCESS);
        }

        public CustomizedStorer(int type) {
            this.type = type;
        }

        @Override
        public StoreResult store(TaskStoreInfo taskStoreInfo) {
            System.err.println("【SystemErrorTaskStorer】 Store");
            switch (type) {
                case TYPE_SUCCESS:
                    result = TYPE_SUCCESS;
                    return StoreResult.successed(STORE_KEY);
                case TYPE_FAIL:
                    result = TYPE_FAIL;
                    return StoreResult.failed();
                case  TYPE_EXCEPTION:
                    result = TYPE_EXCEPTION;
                    throw new StorerException();
            }
            throw new Error();
        }

        public int result() {
            return result;
        }
    }

    private static class StorerException extends RuntimeException {
    }

    private static class TaskCallback extends TaskExecutorListenerAdapter {
        @Override
        public void onSuccess(Task taskInfo) {
            System.out.println("【TaskCallback】Success ");
        }

        @Override
        public TaskListenerResult onFail(Task taskInfo, Throwable throwable) {
            System.err.println("【TaskCallback】OnFail ");
            return TaskListenerResult.fail("回调失败测试.");
        }

        @Override
        public String getType() {
            return "TaskCallback";
        }
    }
}