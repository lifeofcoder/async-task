package com.lifeofcoder.asynctask.core.register;

/**
 * 注册中心接口
 *
 * @author xbc
 * @date 2020/1/13
 */
public interface Register<K, V> {
    /**
     * 向注册中心注册对象
     */
    void register(K key, V value);

    /**
     * 是否存在
     */
    boolean exists(K key);

    /**
     * 从注册中心获取对象
     * @param key 对象类型
     * @return
     */
    V get(K key);
}
