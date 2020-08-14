package com.lifeofcoder.asynctask.core.entity;

import com.lifeofcoder.asynctask.core.BusinessCoder;
import com.lifeofcoder.asynctask.core.Task;

/**
 * 异步任务引擎信息
 *
 * @author xbc
 * @date 2020/1/13
 */
public class TaskInfo implements Task {
    private static final long serialVersionUID = 3713652650657311809L;

    /**
     * 任务类型
     */
    private String type;

    /**
     * 业务code，用于唯一标识业务（框架不使用，供业务自己使用）
     */
    private String businessCode;

    /**
     * 任务数据
     */
    private Object data;

    public TaskInfo() {
    }

    public TaskInfo(String type, BusinessCoder data) {
        this(type, null, data);
    }

    public TaskInfo(String type, String businessCode) {
        this(type, businessCode, null);
    }

    public TaskInfo(String type, String businessCode, Object data) {
        if (null == businessCode && data instanceof BusinessCoder) {
            businessCode = ((BusinessCoder) data).getBusinessCode();
        }

        this.type = type;
        this.businessCode = businessCode;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static TaskInfo build(String type, BusinessCoder data) {
        return build(type, data.getBusinessCode(), data);
    }

    public static TaskInfo build(String type, String businessCode) {
        return build(type, businessCode, null);
    }

    public static TaskInfo build(String type, String businessCode, Object data) {
        return new TaskInfo(type, businessCode, data);
    }

    @Override
    public String toString() {
        return "type='" + type + '\'' + ", businessCode='" + businessCode + '\'';
    }
}
