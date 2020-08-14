package com.lifeofcoder.asynctask.core.persistencer;

/**
 * 文件存储器
 *
 * @author xbc
 * @date 2020/1/22
 */
public interface StoreFile<T> {
    /**
     * 追加数据
     * @param data 待追加的数据
     * @return 追加的数据长度
     */
    int append(T data);

    /**
     * 读取数据
     * @param position
     * @return 读取的内容
     */
    T read(int position);

    /**
     * 写入指定大小的内容是否会达到文件尾部
     * @param waitToWroteSize
     * @return
     */
    boolean reachFileEndBy(int waitToWroteSize);
}
