package com.xxl.job.spring.boot.executor;

/**
 * 任务执行路由策略枚举类
 */
public enum ExecutorRouteStrategyEnum {

    /**
     * 第一个: FIRST
     */
    FIRST("第一个"),

    /**
     * 最后一个: LAST
     */
    LAST("最后一个"),

    /**
     * 轮询: ROUND
     */
    ROUND("轮询"),

    /**
     * 随机: RANDOM
     */
    RANDOM("随机"),

    /**
     * 一致性HASH: CONSISTENT_HASH
     */
    CONSISTENT_HASH("一致性HASH"),

    /**
     * 最不经常使用: LEAST_FREQUENTLY_USED
     */
    LEAST_FREQUENTLY_USED("最不经常使用"),

    /**
     * 最近最久未使用: LEAST_RECENTLY_USED
     */
    LEAST_RECENTLY_USED("最近最久未使用"),

    /**
     * 故障转移: FAILOVER
     */
    FAILOVER("故障转移"),

    /**
     * 忙碌转移: BUSYOVER
     */
    BUSYOVER("忙碌转移"),

    /**
     * 分片广播: SHARDING_BROADCAST
     */
    SHARDING_BROADCAST("分片广播");

    private final String desc;

    ExecutorRouteStrategyEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.name() + " -> " + desc;
    }

}
