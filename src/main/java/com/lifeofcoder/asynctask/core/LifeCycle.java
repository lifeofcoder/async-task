package com.lifeofcoder.asynctask.core;

import java.util.concurrent.TimeUnit;

/**
 * 声明周期接口
 *
 * @author xbc
 * @date 2020/1/15
 */
public interface LifeCycle {
    enum State {
        INITIALIZING,
        INITIALIZED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED
    }

    State getState();
    void initialize();
    boolean isStarted();
    boolean isStopped();
    void start();
    void stop();
    void stop(final long timeout, final TimeUnit timeUnit);
}
