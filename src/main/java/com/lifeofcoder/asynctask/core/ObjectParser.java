package com.lifeofcoder.asynctask.core;

/**
 * 对象解析器：将对象在字符串（json,xml）之前转换
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface ObjectParser<T> extends Typer {
    /**
     * 将字符串对象内容转换为一个对象
     * @param data 字符串：如json，xml，16进制
     * @return 转换后的对象
     */
    T parseObject(String data, Class<T> type);

    /**
     * 将对象转换为字符串内容
     * @param obj 待转换的对象
     * @return 转换后的字符串内容
     */
    String toString(T obj);
}
