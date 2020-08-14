package com.lifeofcoder.asynctask.core.result;

/**
 * 结果类
 *
 * @author xbc
 * @date 2020/1/13
 */
public abstract class Result {
    private boolean success;

    public Result(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static boolean isSuccess(Result result) {
        return null != result && result.isSuccess();
    }
}