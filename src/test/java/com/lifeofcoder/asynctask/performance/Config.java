package com.lifeofcoder.asynctask.performance;

/**
 *
 *
 * @author xbc
 * @date 2020/2/13
 */
public final class Config {
    public static final int SLEEP_TIME = -1;
    public static final int SINGLE_TASK_NUMS = 20000 * 1000;
    public static final int MULTI_TASK_NUMS = 3000 * 1000;
    public static final int nThreads = 4;
    public static final int ringBufferSize = 1024 * 64;
    public static final int PRODUCERS = 10;
}
