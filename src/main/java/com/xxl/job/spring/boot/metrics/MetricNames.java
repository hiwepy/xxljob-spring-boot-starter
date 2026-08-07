package com.xxl.job.spring.boot.metrics;

import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Micrometer 点分指标名称拼接工具。
 */
public final class MetricNames {

    private MetricNames() {
    }

    /**
     * 将名称片段拼接为点分名称，并忽略空片段。
     *
     * @param name 首个名称片段
     * @param names 后续名称片段
     * @return 点分名称
     */
    public static String name(String name, String... names) {
        StringBuilder builder = new StringBuilder();
        append(builder, name);
        if (Objects.nonNull(names)) {
            for (String part : names) {
                append(builder, part);
            }
        }
        return builder.toString();
    }

    /**
     * 将类名与名称片段拼接为点分名称。
     *
     * @param type 首个名称片段对应的类型
     * @param names 后续名称片段
     * @return 点分名称
     */
    public static String name(Class<?> type, String... names) {
        return name(Objects.requireNonNull(type, "type must not be null").getName(), names);
    }

    private static void append(StringBuilder builder, String part) {
        if (StringUtils.hasLength(part)) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }
}
