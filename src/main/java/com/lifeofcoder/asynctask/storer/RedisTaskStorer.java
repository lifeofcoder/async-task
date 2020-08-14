package com.lifeofcoder.asynctask.storer;

import com.lifeofcoder.asynctask.core.TaskStorer;
import com.lifeofcoder.asynctask.core.entity.TaskStoreInfo;
import com.lifeofcoder.asynctask.core.result.StoreResult;

/**
 * Redis任务存储器
 *
 * @author xbc
 * @date 2020/1/17
 */
public class RedisTaskStorer implements TaskStorer {
    @Override
    public StoreResult store(TaskStoreInfo taskStoreInfo) {
        return null;
    }

    @Override
    public String getType() {
        return "Engine-RedisTaskStorer";
    }
}
