package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.entity.TaskStoreInfo;
import com.lifeofcoder.asynctask.core.result.StoreResult;

/**
 * 任务存储器，当失败的时候时候需要对任务元数据存储起来，让其可以手工执行。
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface TaskStorer extends Typer {
    /**
     * 存储元数据
     */
    StoreResult store(TaskStoreInfo taskStoreInfo);
}
