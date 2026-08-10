package com.xxl.job.spring.boot;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.spring.boot.metrics.XxlJobMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobMetricsAutoConfiguration}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobMetricsAutoConfigurationTest {

    @Test
    void xxlJobMetricsBeanCreated() {
        XxlJobMetricsAutoConfiguration config = new XxlJobMetricsAutoConfiguration();
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        XxlJobMetrics metrics = config.xxlJobMetrics(executor);
        assertThat(metrics).isNotNull();
    }
}
