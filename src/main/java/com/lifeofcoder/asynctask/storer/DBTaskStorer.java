package com.lifeofcoder.asynctask.storer;

import com.lifeofcoder.asynctask.core.TaskStorer;
import com.lifeofcoder.asynctask.core.entity.AsyncTaskException;
import com.lifeofcoder.asynctask.core.entity.TaskStoreInfo;
import com.lifeofcoder.asynctask.core.result.StoreResult;

import java.util.Objects;

/**
 * 数据库(MySQL/Mongo等)任务存储器
 *
 * @author xbc
 * @date 2020/1/17
 */
public class DBTaskStorer implements TaskStorer {
    private TaskInfoDao taskInfoDao;
    public DBTaskStorer(TaskInfoDao taskInfoDao) {
        Objects.requireNonNull(taskInfoDao, "TaskInfoDao can't be null.");
        this.taskInfoDao = taskInfoDao;
    }

    @Override
    public StoreResult store(TaskStoreInfo taskExecutedInfo) {
        try {
            String storeKey = taskInfoDao.insert(taskExecutedInfo);
            return StoreResult.successed(storeKey);
        }
        catch (Exception e) {
            throw new AsyncTaskException("数据库(" + taskInfoDao.getType() + ")归档错误.", e);
        }
    }

    @Override
    public String getType() {
        return "Engine-DBTaskStorer[" + taskInfoDao.getType() + "]";
    }
}
