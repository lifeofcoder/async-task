package com.lifeofcoder.asynctask.performance;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author xbc
 * @date 2020/2/13
 */
public class CallableTask implements Callable<String> {
    @Override
    public String call() throws InterruptedException {
        int sleepTime = Config.SLEEP_TIME;
        if (sleepTime >= 0) {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        }
        else {
            int i = 0;
            while (i < 1000 * 000 * 00000) {
                i++;
            }
        }
        return "Done";
    }
}
