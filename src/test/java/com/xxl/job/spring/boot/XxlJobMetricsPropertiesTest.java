package com.xxl.job.spring.boot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobMetricsProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobMetricsPropertiesTest {

    @Test
    void defaultValues() {
        XxlJobMetricsProperties props = new XxlJobMetricsProperties();
        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getExtraTags()).isNotNull().isEmpty();
    }

    @Test
    void prefix() {
        assertThat(XxlJobMetricsProperties.PREFIX).isEqualTo("xxl.job.metrics");
    }

    @Test
    void settersAndGetters() {
        XxlJobMetricsProperties props = new XxlJobMetricsProperties();
        props.setEnabled(true);
        props.getExtraTags().put("env", "test");

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getExtraTags()).containsEntry("env", "test");
    }
}
