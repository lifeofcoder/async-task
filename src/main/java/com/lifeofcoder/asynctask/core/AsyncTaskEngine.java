package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.entity.*;
import com.lifeofcoder.asynctask.core.impl.DefaultLogAlarmer;
import com.lifeofcoder.asynctask.core.impl.DefaultTaskStorer;
import com.lifeofcoder.asynctask.core.impl.TaskExecutorRegisterImpl;
import com.lifeofcoder.asynctask.core.impl.TaskInfoObjectParser;
import com.lifeofcoder.asynctask.core.register.TaskExecutorRegister;
import com.lifeofcoder.asynctask.core.register.TyperRegister;
import com.lifeofcoder.asynctask.core.result.StoreResult;
import com.lifeofcoder.asynctask.core.result.TaskListenerResult;
import com.lifeofcoder.asynctask.core.util.ValidatorHelper;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 边缘服务引擎
 *
 * @author xbc
 * @date 2020/1/13
 */
public class AsyncTaskEngine extends AbstractExecutorService implements TyperRegister<TaskExecutorHolder>, TaskAppender, LifeCycle, ExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskEngine.class);
    private static final String BATCH_TASK_TYPE = "BATCH_TASK_TYPE";

    /**
     * 执行引擎默认的日志文件(包括：默认的告警输出，默认的原数据存储输出)
     */
    private static final String ASYNC_TASK_ENGINE_LOGGER = "asyncTaskEngineLogger";
    public static final String BIZ_CODE_BATCH_TASK = "BATCH_TASK";

    private AtomicReference<State> stateRef = new AtomicReference<>(State.INITIALIZED);

    /**
     * RingBuffer的大小
     */
    private int ringBufferSize;

    private WaitStrategy waitStrategy;
    /**
     * 生产消息的时候的最大重试次数
     */
    private int maxRetryTimes = Integer.MAX_VALUE;

    private Disruptor<TaskInfoInner> disruptor;
    private RingBuffer<TaskInfoInner> taskRingBuffer;
    private Alarmer alarmer;
    private Alarmer defaultAlarmer = new DefaultLogAlarmer(ASYNC_TASK_ENGINE_LOGGER);

    private TaskStorer taskStorer;
    private TaskStorer defaultTaskStorer = new DefaultTaskStorer(ASYNC_TASK_ENGINE_LOGGER, new TaskInfoObjectParser());

    private TaskExecutorRegister deletateRegistry = new TaskExecutorRegisterImpl();
    private TaskExecutor taskExecutor;

    private ExecutorMode executorMode;
    private int coreThreadSize;

    private AsyncTaskEngine(ExecutorMode executorMode, int coreThreadSize, int ringBufferSize, WaitStrategy waitStrategy) {
        this.ringBufferSize = ringBufferSize;
        this.coreThreadSize = coreThreadSize;
        this.executorMode = executorMode;
        if (null == waitStrategy) {
            waitStrategy = new YieldingWaitStrategy();
        }
        this.waitStrategy = waitStrategy;
    }

    /**
     * 批量任务模式通过这个启动即可。
     */
    public void start() {
        //make sure it starts only once
        if (stateRef.compareAndSet(State.INITIALIZED, State.STARTING)) {
            try {
                validate();
                doStart();
            }
            catch (Throwable throwable) {
                stateRef.set(State.STOPPED);
                throw throwable;
            }
            stateRef.set(State.STARTED);
        }
    }

    /**
     * 启动任务执行引擎，并返回任务添加接口。[边缘任务模式，需要使用这个启动获取到TaskAppender]
     * 启动过程中异常：
     * 1、启动时参数校验失败
     * 2、如果多线程同时启动，第二个线程会抛出异常
     * @throws AsyncTaskException 启动异常
     */
    public TaskAppender startEngine() throws AsyncTaskException {
        start();
        if (stateRef.get() == State.STARTED) {
            return this;
        }

        throw new AsyncTaskException("The AsyncTaskEngine has not started yet. "
                                    + "If you get this error maybe because you started the engine more than once at the same time.");
    }

    private void validate() {
        if (ringBufferSize < 1) {
            throw new AsyncTaskException("The parameter ringBufferSize must be greater than 0");
        }
        if (maxRetryTimes < 1) {
            throw new AsyncTaskException("The parameter maxRetryTimes must be greater than 0");
        }
        if (alarmer == null) {
            LOGGER.warn("Customized alarmer is not set!");
        }
        if (alarmer == null && defaultAlarmer == null) {
            throw new AsyncTaskException("Neither customized alarmer nor default alarmer is set.");
        }
        if (taskStorer == null) {
            LOGGER.warn("Customized taskMetaStorer is not set!");
        }
        if (taskStorer == null && defaultTaskStorer == null) {
            throw new AsyncTaskException("Neither customized taskMetaStorer nor default taskMetaStorer is set.");
        }
    }

    private void doStart() {
        taskExecutor = new TaskExecutorComposite(deletateRegistry, new TaskExecutorComposite.TaskExecuteCallback() {
            @Override
            public void onFail(TaskInfoInner taskInfoInner, TaskExecutedInfo taskExecutedInfo) {
                taskExecutedFail(taskInfoInner, taskExecutedInfo);
            }

            @Override
            public void onSuccess(TaskInfoInner taskInfoInner) {
                taskExecutedSuccess(taskInfoInner);
            }
        });

        disruptor = DisruptorBuilder.<TaskInfoInner>newInstance()
                .setEventFactory(new TaskInfoFactory())
                .setRingBufferSize(ringBufferSize)
                .setWaitStrategy(waitStrategy)
                .setThreadFactory(new NamedThreadFactory("AsyncTaskEngine-Disruptor-", true))
                .setProducerType(ProducerType.MULTI).build();

        if (executorMode == ExecutorMode.EdgeTask) {
            disruptor.handleEventsWith(new TaskInfoEventHandler());
        }
        else {
            initWorkPool();
    }
        //        disruptor.handleEventsWithWorkerPool();
        disruptor.setDefaultExceptionHandler(new LogAndAlarmExceptionHandler());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                AsyncTaskEngine.this.stop();
            }
        });

        taskRingBuffer = this.disruptor.start();
    }

    private void initWorkPool() {
        //添加处理器
        BatchTaskWorkPool[] batchTaskWorkPools = new BatchTaskWorkPool[coreThreadSize];
        for (int i = 0; i < coreThreadSize; i++) {
            batchTaskWorkPools[i] = new BatchTaskWorkPool();
        }
        disruptor.handleEventsWithWorkerPool(batchTaskWorkPools);
    }

    @Override
    public void addTask(TaskInfo taskInfo) throws AddAsyncTaskException {
        //服务器停止之后，或者停止的过程中，不允许向disruptor中添加任务。
        // 特别是在其停止的过程中，否则会造成disruptor永远无法停止
        addTask(taskInfo, null);
    }

    @Override
    public void addTask(TaskInfo taskInfo, TaskExecutorListener taskExecutorListener) throws AddAsyncTaskException {
        State state = stateRef.get();
        if (state != State.STARTED) {
            throw new AddAsyncTaskException("The AsyncTaskEngine has not started. Its state is " + state);
        }
        validateTaskInfo(taskInfo);
        publishEvent(new TaskEventTranslator(taskInfo, taskExecutorListener));
    }

    private void addTask(Runnable runnable, TaskExecutorListener taskExecutorListener) {
        State state = stateRef.get();
        if (state != State.STARTED) {
            throw new AddAsyncTaskException("The AsyncTaskEngine has not been started. Its state is " + state);
        }
        publishEvent(new RunnableEventTranslator(runnable, taskExecutorListener));
    }

    private void publishEvent(EventTranslator<TaskInfoInner> eventTranslator) {
        taskRingBuffer.publishEvent(eventTranslator);
        int retryNums = 0;
        //        while (true) {
        //            if (taskRingBuffer.tryPublishEvent(new TaskEventTranslator(taskInfo, taskExecutorListener))) {
        //                if (LOGGER.isDebugEnabled()) {
        //                    LOGGER.debug("Succeeded to add task[" + taskInfo + "] into AsyncTaskEngine.");
        //                }
        //                return;
        //            }
        //
        //            if (++retryNums > maxRetryTimes) {
        //                TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(taskInfo)
        //                        .setSysErrorMsg(Messager.TOO_BUSY).build();
        //                taskExecutedFail(new TaskInfoInner(taskInfo, taskExecutorListener), taskExecutedInfo);
        //            }
        //            if (LOGGER.isDebugEnabled()) {
        //                LOGGER.debug("Retry to add task[" + taskInfo + "] into AsyncTaskEngine. RetryNum:" + retryNums);
        //            }
        //        }
    }

    private void validateTaskInfo(TaskInfo taskInfo) {
        if (null == taskInfo) {
            throw new AddAsyncTaskException("TaskInfo can't be null.");
        }

        if (null == taskInfo.getType() || null == taskInfo.getBusinessCode()) {
            throw new AddAsyncTaskException("Both type and businessCode of TaskInfo are required.");
        }

        if (!deletateRegistry.exists(taskInfo.getType())) {
            throw new AddAsyncTaskException("TaskInfo[" + taskInfo + "] don't have a matched TaskExecutor.");
        }
    }

    @Override
    public void register(TaskExecutorHolder taskExecutorHolder) {
        register(taskExecutorHolder.getType(), taskExecutorHolder);
    }

    @Override
    public void register(String type, TaskExecutorHolder taskExecutorHolder) {
        //这里的注册中心使用的普通HashMap实现，因为所有新增操作都是在初始化的时候（单线程）完成的，
        // 后续都是高并发的get操作，所以不存在并发问题
        deletateRegistry.register(type, taskExecutorHolder);
    }

    @Override
    public boolean exists(String type) {
        return deletateRegistry.exists(type);
    }

    @Override
    public TaskExecutorHolder get(String type) {
        return deletateRegistry.get(type);
    }

    private void storeAndAlarm(TaskExecutedInfo taskExecutedInfo) {
        StoreResult storeResult = store(taskExecutedInfo);
        alarm(taskExecutedInfo, storeResult);
    }

    private StoreResult store(TaskExecutedInfo taskExecutedInfo) {
        StoreResult storeResult = null;
        try {
            storeResult = store0(taskExecutedInfo);
        }
        catch (Throwable throwable) {
            //ignore
            LOGGER.error("Failed to store task info.", throwable);
        }

        if (null == storeResult) {
            //never happen in normal situation
            //失败则进行告警提醒
            taskExecutedInfo.appendSysErrorMsg(Messager.TASK_STORER_RETURN_NULL);
            storeResult = new StoreResult(false, "");
        }
        else {
            taskExecutedInfo.appendSysErrorMsg(Messager.TASK_STORER_FAILED);
        }

        return storeResult;
    }

    /**
     * 将任务元数据存储起来，并返回当前存储其的type
     */
    private StoreResult store0(TaskExecutedInfo taskExecutedInfo) {
        if (null == taskStorer) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("There is no customized task storer. Try to use default task meta storer:" + defaultTaskStorer.getType());
            }
            return doStrore(defaultTaskStorer, taskExecutedInfo);
        }

        StoreResult storeResult = null;
        try {
            //to execute customized task storer.
            storeResult = doStrore(taskStorer, taskExecutedInfo);
        }
        catch (Throwable throwable) {
            //ignore
            LOGGER.error("Failed to use customized storer[" + taskStorer.getType() + "] to store task info.", throwable);
        }
        if (StoreResult.isSuccess(storeResult)) {
            return storeResult;
        }

        LOGGER.info("Failed to store task info by using customized storer[" + taskStorer.getType() + "]. Try to use default storer.");
        if (defaultTaskStorer == null) {
            LOGGER.error("There is no default task storer to used while the customized storer storing is failed.");
            return storeResult;
        }

        storeResult = doStrore(defaultTaskStorer, taskExecutedInfo);
        return storeResult;
    }

    private StoreResult doStrore(TaskStorer taskStorer, TaskExecutedInfo taskExecutedInfo) {
        if (null == taskStorer) {
            return null;
        }

        StoreResult storeResult = taskStorer.store(new TaskStoreInfo(taskExecutedInfo));
        storeResult.setType(taskStorer.getType());
        return storeResult;
    }

    /**
     * 告警，告警方法不能抛出异常。否则可能会导致死循环。
     * @param taskExecutedInfo 任务错误相关信息
     * @param storeResult 存储归档结果，可为null
     */
    private void alarm(TaskExecutedInfo taskExecutedInfo, StoreResult storeResult) {
        //nerver happen
        Objects.requireNonNull(taskExecutedInfo, "TaskErrorInfo can't be null.");

        StringBuilder alarmMsgBuilder = new StringBuilder(Messager.ALARM_HEADER);

        if (ValidatorHelper.isNotEmpty(taskExecutedInfo.getIp())) {
            alarmMsgBuilder.append(Messager.format(Messager.ALARM_MSG_IP, taskExecutedInfo.getIp()));
        }
        if (ValidatorHelper.isNotEmpty(taskExecutedInfo.getSysErrorMsg())) {
            alarmMsgBuilder.append(Messager.format(Messager.ALARM_MSG_SYSTEM_ERROR, taskExecutedInfo.getSysErrorMsg()));
        }
        if (ValidatorHelper.isNotEmpty(taskExecutedInfo.getBizErrorMsg())) {
            alarmMsgBuilder.append(Messager.format(Messager.ALARM_MSG_BIZ_ERROR, taskExecutedInfo.getBizErrorMsg()));
        }
        if (null != taskExecutedInfo.getException()) {
            alarmMsgBuilder.append(Messager.format(Messager.ALARM_MSG_EXCEPTION, taskExecutedInfo.getException().getClass().getSimpleName()));
        }
        if (null != storeResult) {
            //归档key， 归档类
            alarmMsgBuilder.append(Messager.format(Messager.ALARM_MSG_STORE_RESULT, storeResult.getStoreKey(), storeResult.getType()));
        }

        alarm0(alarmMsgBuilder.toString());
    }

    /**
     * 告警是引擎的最后兜底方案了，其执行抛出异常或者失败，则没有办法解决。只有打印日志了。且不必再向外抛出任何异常
     */
    private void alarm0(String alarmMsg) {
        try {
            boolean succeeded = doAlarm(alarmMsg);
            if (!succeeded) {
                LOGGER.error("[AsyncTaskEngine]Failed to alarm. The message to be alarmed is " + alarmMsg);
            }
        }
        catch (Throwable throwable) {
            //ignore
            LOGGER.error("[AsyncTaskEngine]Failed to alarm. The message to be alarmed is " + alarmMsg, throwable);
        }
    }

    private boolean doAlarm(String message) {
        if (null == alarmer) {
            return defaultAlarm(message);
        }

        try {
            if (alarmer.alarm(message)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            LOGGER.error("Failed to use customized alarmer[" + alarmer.getType() + "] to alarm!", throwable);
        }

        return  defaultAlarm("自定义Alarmer[" + alarmer.getType() + "]告警失败！告警内容：" + message);
    }

    private boolean defaultAlarm(String message) {
        if (null == alarmer) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("There is no customized alarmer! Try to use default alarmer to alarm.");
            }
        }
        else {
            LOGGER.info("Failed to use customized alarmer[" + alarmer.getType() + "] to alarm! Try to use default alarmer.");
        }

        if (null == defaultAlarmer) {
            LOGGER.error("There is no default alarmer to used while the customized alarmer alarming is failed.");
            return false;
        }

        return defaultAlarmer.alarm(message);
    }

    private void executeRunnableDirectly(TaskInfoInner<Runnable> taskInfoInner) {
        try {
            taskInfoInner.getData().run();
        }
        catch (Throwable throwable) {
            //never happen
            //通过AbstractExecutorService实现的，需要返回Feature的任务会被封装成FeatureTask，其不会抛出异常
            //普通runnable通过execute执行就会直接抛出异常
            throw new AsyncTaskFatalException("Runnable threw an exception.", throwable);
        }
    }

    private void processTask(TaskInfoInner taskInfoInner, long sequence, boolean endOfBatch) {
        try {
            taskExecutor.execute(taskInfoInner, 0);
        }
        finally {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("业务[" + taskInfoInner + "]执行完成!");
            }
            if (null != taskInfoInner.getTaskExecutorListener()) {
                taskInfoInner.getTaskExecutorListener().onComplete(taskInfoInner);
            }

            //执行完成后reset, help gc
            taskInfoInner.resest();
        }
    }

    private void taskExecutedSuccess(TaskInfoInner taskInfo) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Task[" + taskInfo + "] has been executed successfully.");
        }
        if (null != taskInfo.getTaskExecutorListener()) {
            try {
                taskInfo.getTaskExecutorListener().onSuccess(taskInfo);
            }
            //抛出异常会导致该任务终止
            catch (Throwable e) {
                throw new AsyncTaskFatalException(Messager.SUCCESS_CALLBACK_EXCEPTION, e);
            }
        }
    }

    private void taskExecutedFail(TaskInfoInner taskInfoInner, TaskExecutedInfo taskExecutedInfo) {
        String errorMsg = "Failed to handle task[" + taskInfoInner + "]!!! The reason is " + taskExecutedInfo;
        if (null != taskExecutedInfo.getException()) {
            LOGGER.error(errorMsg, taskExecutedInfo.getException());
        }
        else {
            LOGGER.error(errorMsg);
        }

        TaskExecutorListener taskExecutorListener = taskInfoInner.getTaskExecutorListener();
        if (null != taskExecutorListener) {
            TaskListenerResult result;
            //回调异常，需要告警。所以需要先处理异常，然后alarm
            try {
                result = taskExecutorListener.onFail(taskInfoInner, taskExecutedInfo.getException());
            }
            catch (Exception e) {
                LOGGER.error("Failed to execute onFail method of TaskExecutorListener[" + taskExecutorListener.getType() + ", " + taskExecutorListener.getClass().getSimpleName() + "].", e);
                result = TaskListenerResult.exception(e, "Method onFail of TaskExecutorListener[" + taskExecutorListener.getType() + "] threw an exception.");
            }

            if (TaskListenerResult.isSuccess(result)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Business has an executing error of task[" + taskInfoInner + "] successfully.");
                }
                return;
            }
            else {
                taskExecutedInfo.appendBizErrorMsg("回调错误:" + TaskListenerResult.getMessage(result));
            }
        }

        storeAndAlarm(taskExecutedInfo);
    }

    @Override
    public void stop() {
        stop(-1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop(long timeout, TimeUnit timeUnit) {
        //in case it is shut down more than once at the same time.
        if (stateRef.compareAndSet(State.STARTED, State.STOPPING)) {
            disruptor.shutdown();
            stateRef.set(State.STOPPED);
        }
    }

    @Override
    public State getState() {
        return stateRef.get();
    }

    @Override
    public void initialize() {
        //not used.
    }

    @Override
    public boolean isStarted() {
        return stateRef.get() == State.STARTED;
    }

    @Override
    public boolean isStopped() {
        return stateRef.get() == State.STOPPED;
    }

    public void setDefaultAlarmer(Alarmer defaultAlarmer) {
        this.defaultAlarmer = defaultAlarmer;
    }

    public void setDefaultTaskStorer(TaskStorer defaultTaskStorer) {
        this.defaultTaskStorer = defaultTaskStorer;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public void setAlarmer(Alarmer alarmer) {
        this.alarmer = alarmer;
    }

    public void setTaskStorer(TaskStorer taskStorer) {
        this.taskStorer = taskStorer;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * ExecutorServiceImpl
     */
    @Override
    public void shutdown() {
        stop();
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new NotSupportedException();
    }

    @Override
    public boolean isShutdown() {
        return isStopped();
    }

    @Override
    public boolean isTerminated() {
        //disruptor只能够发起关闭，但是并不能判断是否已经完全关闭了(终止)
        throw new NotSupportedException();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new NotSupportedException();
    }

    @Override
    public void execute(Runnable command) {
        Objects.requireNonNull(command, "command can't be null.");
        //可以有任务自己实现TaskExecutorListener
        if (command instanceof TaskExecutorListener) {
            addTask(command, (TaskExecutorListener) command);
        }
        else {
            addTask(command, null);
        }
    }

    public static AsyncTaskEngineBuilder buildEdgeTask(int ringBufferSize) {
        return new AsyncTaskEngineBuilder(ExecutorMode.EdgeTask, 1, ringBufferSize, new YieldingWaitStrategy());
    }

    public static AsyncTaskEngineBuilder buildEdgeTask(int ringBufferSize, WaitStrategy waitStrategy) {
        return new AsyncTaskEngineBuilder(ExecutorMode.EdgeTask, 1, ringBufferSize, waitStrategy);
    }

    public static AsyncTaskEngineBuilder buildBatchTask(int coreThreadSize, int ringBufferSize) {
        if (coreThreadSize < 1) {
            throw new IllegalArgumentException("coreThreadSize must be larger than 0.");
        }
        return new AsyncTaskEngineBuilder(ExecutorMode.BatchTask, coreThreadSize, ringBufferSize, new YieldingWaitStrategy());
    }

    public static AsyncTaskEngineBuilder buildBatchTask(int coreThreadSize, int ringBufferSize,  WaitStrategy waitStrategy) {
        if (coreThreadSize < 1) {
            throw new IllegalArgumentException("coreThreadSize must be larger than 0.");
        }
        return new AsyncTaskEngineBuilder(ExecutorMode.BatchTask, coreThreadSize, ringBufferSize, waitStrategy);
    }

    @Override
    public String getType() {
        return AsyncTaskEngine.class.getSimpleName();
    }

    private static class TaskInfoFactory implements com.lmax.disruptor.EventFactory<TaskInfoInner> {
        @Override
        public TaskInfoInner newInstance() {
            TaskInfoInner taskInfoInner = new TaskInfoInner();
            taskInfoInner.setTaskInfo(new TaskInfo());
            return taskInfoInner;
        }
    }

    private class BatchTaskWorkPool implements WorkHandler<TaskInfoInner> {
        @Override
        public void onEvent(TaskInfoInner event) throws Exception {
            executeRunnableDirectly(event);
        }
    }

    private class TaskInfoEventHandler implements com.lmax.disruptor.EventHandler<TaskInfoInner> {
        @Override
        public void onEvent(TaskInfoInner taskInfoInner, long sequence, boolean endOfBatch) throws Exception {
            processTask(taskInfoInner, sequence, endOfBatch);
        }
    }

    //异常执行器抛出异常会导致EventProcessor终止，所以我们不抛出任何异常
    private final class LogAndAlarmExceptionHandler implements ExceptionHandler<TaskInfoInner> {
        @Override
        public void handleEventException(Throwable ex, long sequence, TaskInfoInner event) {
            String message = "[AsyncTaskEngine-UncaughtException]执行任务异常！";
            LOGGER.error(message, ex);
            try {
                TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(event).setException(ex).setSysErrorMsg(message).build();
                storeAndAlarm(taskExecutedInfo);
            }
            catch (Throwable throwable) {
                //ignore 防止BatchEventProcessor终止执行
                LOGGER.error("[AsyncTaskEngine-UncaughtException]归档UncaughtException异常.", throwable);
            }
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            String errorMsg = "[AsyncTaskEngine-Exception]启动引擎异常！";
            LOGGER.error(errorMsg, ex);
            try {
                TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(null).setException(ex).setSysErrorMsg(errorMsg).build();
                alarm(taskExecutedInfo, null);
            }
            catch (Throwable throwable) {
                //ignore
                LOGGER.error("[AsyncTaskEngine-UncaughtException]归档UncaughtException异常.", throwable);
            }
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            String errorMsg = "[AsyncTaskEngine-Exception]关闭引擎异常！";
            LOGGER.error(errorMsg, ex);
            try {
                TaskExecutedInfo taskExecutedInfo = TaskExecutedInfo.builder(null).setException(ex).setSysErrorMsg(errorMsg).build();
                alarm(taskExecutedInfo, null);
            }
            catch (Throwable throwable) {
                //ignore
                LOGGER.error("[AsyncTaskEngine-UncaughtException]归档UncaughtException异常.", throwable);
            }
        }
    }

    private static class RunnableEventTranslator implements EventTranslator<TaskInfoInner> {
        private Runnable runnable;
        private TaskExecutorListener taskExecutorListener;

        public RunnableEventTranslator(Runnable runnable, TaskExecutorListener taskExecutorListener) {
            this.runnable = runnable;
            this.taskExecutorListener = taskExecutorListener;
        }

        @Override
        public void translateTo(TaskInfoInner event, long sequence) {
            //不重新new TaskInfo，提高并发性能
            event.setTaskExecutorListener(taskExecutorListener);
            TaskInfo delegateTask = (TaskInfo) event.getDelegateTask();
            delegateTask.setType(BATCH_TASK_TYPE);
            delegateTask.setBusinessCode(BIZ_CODE_BATCH_TASK);
            delegateTask.setData(runnable);
        }
    }

    private static class TaskEventTranslator implements EventTranslator<TaskInfoInner> {
        private TaskInfo taskInfo;
        private TaskExecutorListener taskExecutorListener;

        public TaskEventTranslator(TaskInfo taskInfo, TaskExecutorListener taskExecutorListener) {
            this.taskInfo = taskInfo;
            this.taskExecutorListener = taskExecutorListener;
        }

        @Override
        public void translateTo(TaskInfoInner event, long sequence) {
            event.from(taskInfo, taskExecutorListener);
        }
    }

    public static final class AsyncTaskEngineBuilder implements Builder<AsyncTaskEngine> {
        private AsyncTaskEngine asyncTaskEngine;

        private AsyncTaskEngineBuilder(ExecutorMode executorMode, int coreThreadSize, int ringBufferSize, WaitStrategy waitStrategy){
            asyncTaskEngine = new AsyncTaskEngine(executorMode, coreThreadSize, ringBufferSize, waitStrategy);
        }

        public AsyncTaskEngineBuilder setMaxRetryTimes(int maxRetryTimes) {
            asyncTaskEngine.setMaxRetryTimes(maxRetryTimes);
            return this;
        }

        public AsyncTaskEngineBuilder setAlarmer(Alarmer alarmer) {
            validateSuppotedMode("alarmer", ExecutorMode.EdgeTask);
            asyncTaskEngine.setAlarmer(alarmer);
            return this;
        }

        public AsyncTaskEngineBuilder setTaskStorer(TaskStorer taskStorer) {
            validateSuppotedMode("taskStorer", ExecutorMode.EdgeTask);
            asyncTaskEngine.setTaskStorer(taskStorer);
            return this;
        }

        public AsyncTaskEngineBuilder setDefaultAlarmer(Alarmer defaultAlarmer) {
            validateSuppotedMode("defaultAlarmer", ExecutorMode.EdgeTask);
            asyncTaskEngine.setDefaultAlarmer(defaultAlarmer);
            return this;
        }

        public AsyncTaskEngineBuilder setDefaultTaskMetaStorer(TaskStorer defaultTaskStorer) {
            validateSuppotedMode("defaultTaskStorer", ExecutorMode.EdgeTask);
            asyncTaskEngine.setDefaultTaskStorer(defaultTaskStorer);
            return this;
        }

        public AsyncTaskEngineBuilder registerTaskExecutor(TaskExecutorHolder taskExecutorHolder) {
            validateSuppotedMode("registerTaskExecutor", ExecutorMode.EdgeTask);
            asyncTaskEngine.register(taskExecutorHolder);
            return this;
        }

        public AsyncTaskEngine build() {
            AsyncTaskEngine engine = asyncTaskEngine;
            asyncTaskEngine = null;
            return engine;
        }

        /**
         * 校验参数支持的执行模式
         * @param paramName 参数名
         * @param supportedMode 支持的模式，如果为null表示不限制
         */
        private void validateSuppotedMode(String paramName, ExecutorMode supportedMode) {
            if (null == supportedMode) {
                return;
            }

            ExecutorMode currentMode = asyncTaskEngine.executorMode;
            if (supportedMode == currentMode) {
                return;
            }

            throw new IllegalArgumentException(paramName + " can't be suppored in executor mode[" + currentMode + "].");
        }
    }

    /**
     * 执行模式
     */
    private enum ExecutorMode {
        EdgeTask //边缘任务，使用单线程执行，支持归档，告警
        , BatchTask //批量任务，支持多线程批量执行，不支持归档和告警
    }
}