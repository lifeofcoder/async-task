package com.lifeofcoder.asynctask.core.entity;

import com.lifeofcoder.asynctask.core.Builder;
import com.lifeofcoder.asynctask.core.Task;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * 任务执行信息(执行结果)
 *
 * @author xbc
 * @date 2020/1/13
 */
public class TaskExecutedInfo implements Serializable {
    private static final long serialVersionUID = -3636503159476791670L;

    private static final String DELIMITER = " >> ";

    /**
     * 相关任务信息
     */
    private Task taskInfo;

    /**
     * 业务侧的错误信息(由业务执行监听返回)
     */
    private String bizErrorMsg;

    /**
     * 系统错误消息
     */
    private String sysErrorMsg;

    /**
     * 执行异常
     */
    private Throwable exception;

    private String ip;

    private TaskExecutedInfo(Task taskInfo) {
        this.taskInfo = taskInfo;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            this.ip = localHost.getHostAddress();
        }
        catch (Throwable throwable) {
            //ignore
        }
    }

    public Task getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(Task taskInfo) {
        this.taskInfo = taskInfo;
    }

    public String getBizErrorMsg() {
        return bizErrorMsg;
    }

    public void setBizErrorMsg(String bizErrorMsg) {
        this.bizErrorMsg = bizErrorMsg;
    }

    public String getSysErrorMsg() {
        return sysErrorMsg;
    }

    public void setSysErrorMsg(String sysErrorMsg) {
        this.sysErrorMsg = sysErrorMsg;
    }

    /**
     * 在系统错误消息的尾部追加错误信息
     */
    public void appendSysErrorMsg(String message) {
        if (null == message) {
            return;
        }

        if (null == sysErrorMsg) {
            sysErrorMsg = message;
        }
        else {
            sysErrorMsg = sysErrorMsg + DELIMITER + message;
        }
    }

    public void appendBizErrorMsg(String message) {
        if (null == message) {
            return;
        }

        if (null == bizErrorMsg) {
            bizErrorMsg = message;
        }
        else {
            bizErrorMsg = bizErrorMsg + DELIMITER + message;
        }
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "TaskStoreAndAlarmInfo{" + "taskInfo=" + taskInfo + ", businessErrorMsg='" + bizErrorMsg + '\'' + ", systemErrorMsg='" + sysErrorMsg + '\'' + ", exception=" + exception + '}';
    }

    public static InfoBuilder builder(Task task) {
        return new InfoBuilder(task);
    }

    public static class InfoBuilder implements Builder<TaskExecutedInfo> {
        private TaskExecutedInfo taskExecutedInfo;

        private InfoBuilder(Task task) {
            taskExecutedInfo = new TaskExecutedInfo(task);
        }

        public InfoBuilder setBizErrorMsg(String businessErrorMsg) {
            taskExecutedInfo.setBizErrorMsg(businessErrorMsg);
            return this;
        }

        public InfoBuilder setSysErrorMsg(String systemErrorMsg) {
            taskExecutedInfo.setSysErrorMsg(systemErrorMsg);
            return this;
        }

        public InfoBuilder setException(Throwable exception) {
            taskExecutedInfo.setException(exception);
            return this;
        }

        @Override
        public TaskExecutedInfo build() {
            TaskExecutedInfo tmp = taskExecutedInfo;
            taskExecutedInfo = null; //help gc
            return tmp;
        }
    }
}
