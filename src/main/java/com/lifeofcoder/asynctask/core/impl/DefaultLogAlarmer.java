package com.lifeofcoder.asynctask.core.impl;

import com.lifeofcoder.asynctask.core.Alarmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的告警器：日志告警器
 *
 * @author xbc
 * @date 2020/1/13
 */
public class DefaultLogAlarmer extends DefaultTyper implements Alarmer {
    private final Logger logger;
    private String logName;
    public DefaultLogAlarmer(String logName) {
        this.logName = logName;
        logger = LoggerFactory.getLogger(this.logName);
    }

    @Override
    public boolean alarm(String message) {
        logger.error("[DefaultAlarmer]" + message);
        return true;
    }
}
