package com.lifeofcoder.asynctask.core.util;

import java.util.Collection;
import java.util.Map;

/**
 * 校验帮助类
 *
 * @author xbc
 * @version v1.0
 * @date 2018-01-22
 */
public final class ValidatorHelper {
    private ValidatorHelper() {
    }

    /**
     * Check if the String is empty
     *
     * @param string
     * @return Boolean
     */
    public static boolean isEmpty(String string) {
        return null == string || string.trim().isEmpty();
    }

    /**
     * Check if the String is not empty
     *
     * @param string
     * @return Boolean
     */
    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    /**
     * Check if the Collection is empty
     *
     * @param c
     * @return Boolean
     */
    public static boolean isEmpty(Collection<?> c) {
        return null == c || c.isEmpty();
    }

    /**
     * Check if the Collection is not empty
     *
     * @param c
     * @return Boolean
     */
    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    /**
     * Check if the map is empty
     *
     * @param map
     * @return Boolean
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * Check if the map is not empty
     *
     * @param map
     * @return Boolean
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 如果为null则返回空指针一次
     * @param obj
     * @param errorMsg
     * @param <T>
     * @return
     */
    public static <T> T requireNonNull(T obj, String errorMsg) {
        if (obj == null) {
            throw new NullPointerException(errorMsg);
        }
        return obj;
    }
}
