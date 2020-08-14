package com.lifeofcoder.asynctask.core.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 任务存储信息
 *
 * @author xbc
 * @date 2020/2/7
 */
public class TaskStoreInfo implements Serializable {
    private static final long serialVersionUID = 4131534483925272377L;
    /**
     * 相关任务信息
     */
    private String taskInfo;

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
    private String exception;

    private String ip;

    public TaskStoreInfo(TaskExecutedInfo taskExecutedInfo) {
        setBizErrorMsg(taskExecutedInfo.getBizErrorMsg());
        setException(JSON.toJSONString(taskExecutedInfo.getException()));
        setIp(taskExecutedInfo.getIp());
        setSysErrorMsg(taskExecutedInfo.getSysErrorMsg());
        setTaskInfo(JSON.toJSONString(taskExecutedInfo.getTaskInfo()));
    }

    public String getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(String taskInfo) {
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

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
