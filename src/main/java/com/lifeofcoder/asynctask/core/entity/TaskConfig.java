package com.lifeofcoder.asynctask.core.entity;

import com.lifeofcoder.asynctask.core.Typer;

/**
 * 任务配置
 *
 * @author xbc
 * @date 2020/1/14
 */
public class TaskConfig implements Typer {
    /**
     * 任务类型
     */
    private String type;

    /**
     * 任务最大重试次数
     */
    private int maxRetryTimes;

    public TaskConfig(String type, int maxRetryTimes) {
        this.type = type;
        this.maxRetryTimes = maxRetryTimes;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
