package com.xxl.job.spring.boot.metrics;

import com.xxl.job.core.annotation.XxlJobCron;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MetricMethodJobHandler} 生命周期与指标采集测试。
 */
class MetricMethodJobHandlerTests {

    private MeterRegistry registry;
    private boolean initialized;
    private boolean destroyed;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    @XxlJob("legacyName")
    @XxlJobCron(value = "cronName", cron = "0/10 * * * * ?")
    public void annotatedMethod() {
    }

    public void plainMethod() {
    }

    public void failingMethod() {
        throw new IllegalStateException("boom");
    }

    public void initMethod() {
        initialized = true;
    }

    public void destroyMethod() {
        destroyed = true;
    }

    @Test
    void shouldRecordMetricsWithCronAnnotationPriority() throws Exception {
        MetricMethodJobHandler handler = handler("annotatedMethod", null, null);

        handler.execute();

        assertThat(registry.find(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_SUBMITTED)
                .tag("job", "cronName").counter().count()).isEqualTo(1.0);
        assertThat(registry.find(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING)
                .tag("job", "cronName").gauge().value()).isZero();
        assertThat(registry.find(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED)
                .tag("job", "cronName").counter().count()).isEqualTo(1.0);
        assertThat(registry.find(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_DURATION)
                .tag("job", "cronName").timer().totalTime(TimeUnit.NANOSECONDS)).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldRecordCompletionWhenInvocationFails() throws Exception {
        MetricMethodJobHandler handler = handler("failingMethod", null, null);

        assertThatThrownBy(handler::execute)
                .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class);

        assertThat(registry.find(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING)
                .tag("job", "failingMethod").gauge().value()).isZero();
        assertThat(registry.find(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED)
                .tag("job", "failingMethod").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldInvokeLifecycleMethods() throws Exception {
        Method initMethod = getClass().getMethod("initMethod");
        Method destroyMethod = getClass().getMethod("destroyMethod");
        MetricMethodJobHandler handler = handler("plainMethod", initMethod, destroyMethod);

        handler.init();
        handler.destroy();

        assertThat(initialized).isTrue();
        assertThat(destroyed).isTrue();
    }

    private MetricMethodJobHandler handler(String methodName, Method initMethod, Method destroyMethod)
            throws NoSuchMethodException {
        Method method = getClass().getMethod(methodName);
        return new MetricMethodJobHandler(
                registry, this, method, initMethod, destroyMethod, Collections.emptyList());
    }
}
