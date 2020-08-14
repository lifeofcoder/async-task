package com.lifeofcoder.asynctask.core;

/**
 * 告警器
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface Alarmer extends Typer {
    /**
     * 告警
     * @param message 消息内容
     * @return 成功与否
     */
    boolean alarm(String message);
}
