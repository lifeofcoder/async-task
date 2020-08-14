package com.lifeofcoder.asynctask.storer;

import com.lifeofcoder.asynctask.core.Typer;
import com.lifeofcoder.asynctask.core.entity.TaskStoreInfo;

/**
 * 任务信息dao接口
 *
 * @author xbc
 * @date 2020/1/17
 */
public interface TaskInfoDao extends Typer {
    /**
     * 将任务信息插入数据库 地方
     * @param taskStoreInfo
     * @return
     */
    String insert(TaskStoreInfo taskStoreInfo) throws Exception;

    /**
     * 用于标记当前是什么数据库类型
     */
    @Override
    String getType();
}
