package com.lifeofcoder.asynctask.core.impl;

import com.lifeofcoder.asynctask.core.Typer;

/**
 * 默认的类型命名：使用类名
 *
 * @author xbc
 * @date 2020/1/13
 */
public class DefaultTyper implements Typer {
    @Override
    public String getType() {
        return getClass().getSimpleName();
    }
}
