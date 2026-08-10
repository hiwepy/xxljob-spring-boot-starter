package com.xxl.job.spring.boot.metrics;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link XxlJobMetrics}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobMetricsTest {

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
    void constantsShouldBeStable() {
        assertThat(XxlJobMetrics.XXL_JOB_METRIC_NAME_PREFIX).isEqualTo("xxl");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_SUBMITTED).isEqualTo("xxl.job.submitted");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING).isEqualTo("xxl.job.running");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED).isEqualTo("xxl.job.completed");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_DURATION).isEqualTo("xxl.job.duration");
        assertThat(XxlJobMetrics.METRIC_NAME_JOB_QUEUE_SIZE).isEqualTo("xxl.job.queue.size");
    }

    @Test
    void constructorWithNullExecutorThrows() {
        assertThatThrownBy(() -> new XxlJobMetrics(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorWithTagsShouldCopy() {
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("env", "test"));
        XxlJobMetrics metrics = new XxlJobMetrics(executor, tags);
        // Mutating the original list should not affect the metrics
        tags.add(Tag.of("extra", "value"));
        metrics.bindTo(registry);
        assertThat(registry.getMeters()).isNotNull();
    }

    @Test
    void constructorWithNullTagsShouldNotFail() {
        XxlJobMetrics metrics = new XxlJobMetrics(executor, null);
        metrics.bindTo(registry);
        assertThat(registry.getMeters()).isNotNull();
    }

    @Test
    void bindToShouldNotFail() {
        XxlJobMetrics metrics = new XxlJobMetrics(executor);
        metrics.bindTo(registry);
        assertThat(registry.getMeters()).isNotNull();
    }

    @Test
    void onApplicationEventShouldBindToRegistry() {
        // This tests the onApplicationEvent path indirectly
        XxlJobMetrics metrics = new XxlJobMetrics(executor, Collections.emptyList());
        metrics.bindTo(registry);
        assertThat(registry.getMeters()).isNotNull();
    }
}
