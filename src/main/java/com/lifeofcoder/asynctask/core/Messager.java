package com.lifeofcoder.asynctask.core;

/**
 * 消息容器
 *
 * @author xbc
 * @date 2020/1/13
 */
public final class Messager {
    public static final String ALARM_HEADER = "[异步任务引擎告警]";

    public static final String TOO_BUSY = "边缘执行引擎繁忙，任务太多无法执行.";

    public static final String ALARM_MSG_IP = "【IP】%s ";

    public static final String ALARM_MSG_SYSTEM_ERROR = "【系统错误】%s ";

    public static final String ALARM_MSG_BIZ_ERROR = "【业务错误】%s ";

    public static final String ALARM_MSG_EXCEPTION = "【抛出异常】%s ";

    public static final String ALARM_MSG_STORE_RESULT = "【归档详情】Key=%s, Storer=%s ";

    public static final String REACHED_MAX_TIMES = "超过最大重试次数:%s";

    public static final String SYSTEM_EXCEPTION = "系统异常";

    public static final String SUCCESS_CALLBACK_EXCEPTION = "任务执行成功回调时抛出异常.";

    public static final String NO_TASK_EXECUTOR = "任务[%s]没有对应的任务执行器.";

    public static final String EXECUTE_EXCEPTION = "执行异常";

    public static final String TASK_STORER_RETURN_NULL = "TaskStorer返回NULL";

    public static final String TASK_STORER_FAILED = "任务归档失败";

    public static String format(String msg, Object... params) {
        return String.format(msg, params);
    }
}
