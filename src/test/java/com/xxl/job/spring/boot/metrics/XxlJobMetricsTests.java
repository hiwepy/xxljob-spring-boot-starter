package com.xxl.job.spring.boot.metrics;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XxlJobMetrics} 指标绑定兼容性测试。
 */
class XxlJobMetricsTests {

    private MeterRegistry registry;
    private XxlJobSpringExecutor executor;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        executor = new XxlJobSpringExecutor();
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    @Test
    void shouldKeepMetricNamesStable() {
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_SUBMITTED).isEqualTo("xxl.job.submitted");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING).isEqualTo("xxl.job.running");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED).isEqualTo("xxl.job.completed");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_DURATION).isEqualTo("xxl.job.duration");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_QUEUE_SIZE).isEqualTo("xxl.job.queue.size");
    }

    @Test
    void shouldBindWithoutFailingAcrossCoreVersions() {
        XxlJobMetrics metrics = new XxlJobMetrics(
                executor, Collections.singletonList(Tag.of("executor", "test")));

        metrics.bindTo(registry);

        assertThat(registry.getMeters()).isNotNull();
    }
}
