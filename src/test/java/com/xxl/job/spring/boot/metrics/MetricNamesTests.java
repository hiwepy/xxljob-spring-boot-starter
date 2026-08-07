package com.xxl.job.spring.boot.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MetricNames} 指标名称拼接测试。
 */
class MetricNamesTests {

    @Test
    void shouldJoinNonEmptyParts() {
        assertThat(MetricNames.name("xxl", "job", "duration")).isEqualTo("xxl.job.duration");
    }

    @Test
    void shouldIgnoreNullAndEmptyParts() {
        assertThat(MetricNames.name("xxl", null, "", "job")).isEqualTo("xxl.job");
    }

    @Test
    void shouldUseClassNameAsPrefix() {
        assertThat(MetricNames.name(String.class, "value")).isEqualTo("java.lang.String.value");
    }
}
