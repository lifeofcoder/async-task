package com.lifeofcoder.asynctask.core.register;

import com.lifeofcoder.asynctask.core.Typer;

/**
 * Typer类型的注册器
 *
 * @author xbc
 * @date 2020/1/16
 */
public interface TyperRegister<V extends Typer> extends Register<String, V> {
    void register(V value);
}
