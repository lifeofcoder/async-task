package com.lifeofcoder.asynctask.core;

import com.lifeofcoder.asynctask.core.util.ValidatorHelper;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * Disruptor建造者
 * 参考：Jraft
 *
 * @author xbc
 * @date 2020/1/13
 */
public class DisruptorBuilder<T> implements Builder {
    private EventFactory<T> eventFactory;
    private Integer ringBufferSize;
    private ThreadFactory threadFactory = new NamedThreadFactory("Disruptor-Thread-", true);
    private ProducerType producerType = ProducerType.MULTI;
    private WaitStrategy waitStrategy = new YieldingWaitStrategy();

    private DisruptorBuilder() {
    }

    public static <T> DisruptorBuilder<T> newInstance() {
        return new DisruptorBuilder<>();
    }

    public EventFactory<T> getEventFactory() {
        return this.eventFactory;
    }

    public DisruptorBuilder<T> setEventFactory(final EventFactory<T> eventFactory) {
        this.eventFactory = eventFactory;
        return this;
    }

    public int getRingBufferSize() {
        return this.ringBufferSize;
    }

    public DisruptorBuilder<T> setRingBufferSize(final int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
        return this;
    }

    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    public DisruptorBuilder<T> setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ProducerType getProducerType() {
        return this.producerType;
    }

    public DisruptorBuilder<T> setProducerType(final ProducerType producerType) {
        this.producerType = producerType;
        return this;
    }

    public WaitStrategy getWaitStrategy() {
        return this.waitStrategy;
    }

    public DisruptorBuilder<T> setWaitStrategy(final WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
        return this;
    }

    public Disruptor<T> build() {
        ValidatorHelper.requireNonNull(this.ringBufferSize, " Ring buffer size is required.");
        ValidatorHelper.requireNonNull(this.eventFactory, "Event factory is required.");
        return new Disruptor<>(this.eventFactory, this.ringBufferSize,
                               this.threadFactory, this.producerType, this.waitStrategy);
    }
}
