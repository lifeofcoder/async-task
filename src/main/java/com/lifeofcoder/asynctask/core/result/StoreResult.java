package com.lifeofcoder.asynctask.core.result;

import com.lifeofcoder.asynctask.core.Typer;

/**
 * 存储器的执行结果
 *
 * @author xbc
 * @date 2020/1/13
 */
public class StoreResult extends Result implements Typer {
    /**
     * 存储器，存储完成之后的key，用于消息的查询
     */
    private String storeKey;

    /**
     * 存储器的类型，用于执行引擎告警
     */
    private String type;

    public StoreResult(boolean success, String storeKey) {
        super(success);
        this.storeKey = storeKey;
    }

    public String getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(String storeKey) {
        this.storeKey = storeKey;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    public static StoreResult successed(String storeKey) {
        return new StoreResult(true, storeKey);
    }

    public static StoreResult failed() {
        return new StoreResult(false, null);
    }
}
