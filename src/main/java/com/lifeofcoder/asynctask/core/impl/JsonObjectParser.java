package com.lifeofcoder.asynctask.core.impl;

import com.alibaba.fastjson.JSON;
import com.lifeofcoder.asynctask.core.ObjectParser;

/**
 * Json对象转换器
 *
 * @author xbc
 * @date 2020/1/13
 */
public class JsonObjectParser<T> extends DefaultTyper implements ObjectParser<T> {
    @Override
    public T parseObject(String data, Class<T> type) {
        return JSON.parseObject(data, type);
    }

    @Override
    public String toString(T obj) {
        return JSON.toJSONString(obj);
    }
}
