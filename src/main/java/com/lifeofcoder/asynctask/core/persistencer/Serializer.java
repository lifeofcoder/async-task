package com.lifeofcoder.asynctask.core.persistencer;

/**
 * 序列化
 *
 * @author xbc
 * @date 2020/1/22
 */
public interface Serializer<T> {
    T decode(byte[] buffer);
    byte[] encode(T data);
}
