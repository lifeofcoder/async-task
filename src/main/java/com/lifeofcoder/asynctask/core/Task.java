package com.lifeofcoder.asynctask.core;

import java.io.Serializable;

/**
 * 任务对象接口
 *
 * @author xbc
 * @date 2020/1/14
 */
public interface Task<T> extends Typer, Serializable {
    /**
     * 获取任务携带的数据
     */
    T getData();

    /**
     * 获取任务的业务唯一编码
     */
    String getBusinessCode();
}
