package com.xxl.job.spring.boot.executor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务周期枚举类
 */
public enum ExecutorTriggerPeriodEnum {

    /**
     * 每周: 1
     */
    WEEK("每周"),

    /**
     * 每月: 2
     */
    MONTH("每月"),

    /**
     * 每天: 4
     */
    DAILY("每天");

    private final String desc;

    ExecutorTriggerPeriodEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return name() + " -> " + desc;
    }

}
