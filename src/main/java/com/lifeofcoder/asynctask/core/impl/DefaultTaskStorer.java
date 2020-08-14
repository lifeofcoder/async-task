package com.lifeofcoder.asynctask.core.impl;

import com.lifeofcoder.asynctask.core.ObjectParser;
import com.lifeofcoder.asynctask.core.TaskStorer;
import com.lifeofcoder.asynctask.core.entity.TaskStoreInfo;
import com.lifeofcoder.asynctask.core.result.StoreResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的任务元数据存储器：日志存储器。
 * 即将任务元数据打印到日志中
 *
 * @author xbc
 * @date 2020/1/13
 */
public class DefaultTaskStorer implements TaskStorer {
    private final Logger logger;
    private ObjectParser<TaskStoreInfo> objectParser;
    private String logName;

    public DefaultTaskStorer(String logName, ObjectParser<TaskStoreInfo> objectParser) {
        this.logName = logName;
        this.objectParser = objectParser;
        logger = LoggerFactory.getLogger(this.logName);
    }

    @Override
    public StoreResult store(TaskStoreInfo taskStoreInfo) {
        logger.error("[DefaultTaskMetaStorer]" + objectParser.toString(taskStoreInfo));
        return StoreResult.successed("DefaultTaskMetaStorer");
    }

    @Override
    public String getType() {
        return "DefaultTaskMetaStorer[Logger]";
    }
}