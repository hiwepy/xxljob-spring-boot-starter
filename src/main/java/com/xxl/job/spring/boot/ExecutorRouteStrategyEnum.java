package com.xxl.job.spring.boot;

/**
 * Created by xuxueli on 17/3/10.
 */
public enum ExecutorRouteStrategyEnum {

    FIRST,
    LAST,
    ROUND,
    RANDOM,
    CONSISTENT_HASH,
    LEAST_FREQUENTLY_USED,
    LEAST_RECENTLY_USED,
    FAILOVER,
    BUSYOVER,
    SHARDING_BROADCAST;

}
