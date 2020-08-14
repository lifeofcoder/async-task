package com.lifeofcoder.asynctask.performance;

import org.apache.http.concurrent.BasicFuture;

/**
 *
 *
 * @author xbc
 * @date 2020/2/13
 */
public class MyThread extends Thread {
    private BasicFuture<String> future;
    private Runnable runnable;

    public MyThread(BasicFuture<String> future, Runnable runnable) {
        this.future = future;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
        future.completed("OK");
    }
}
