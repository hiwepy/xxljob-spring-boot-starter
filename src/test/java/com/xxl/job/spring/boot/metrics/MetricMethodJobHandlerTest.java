package com.xxl.job.spring.boot.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MetricMethodJobHandler}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class MetricMethodJobHandlerTest {

    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    public void sampleJobMethod() {
        // no-op
    }

    public void initMethod() {
        // no-op
    }

    public void destroyMethod() {
        // no-op
    }

    @Test
    void constructorShouldRegisterMetrics() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("executor", "test"));

        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, null, tags);

        assertThat(handler).isNotNull();
        // Metrics should be registered
        assertThat(registry.find("xxl.job.submitted").counter()).isNotNull();
        assertThat(registry.find("xxl.job.running").gauge()).isNotNull();
        assertThat(registry.find("xxl.job.completed").counter()).isNotNull();
        assertThat(registry.find("xxl.job.duration").timer()).isNotNull();
    }

    @Test
    void constructorWithNullRegistryThrows() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        assertThatThrownBy(() -> new MetricMethodJobHandler(
                null, this, method, null, null, Collections.emptyList()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorWithNullTargetThrows() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        assertThatThrownBy(() -> new MetricMethodJobHandler(
                registry, null, method, null, null, Collections.emptyList()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorWithNullMethodThrows() {
        assertThatThrownBy(() -> new MetricMethodJobHandler(
                registry, this, null, null, null, Collections.emptyList()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorWithNullTagsShouldWork() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, null, null);
        assertThat(handler).isNotNull();
    }

    @Test
    void executeShouldRecordMetrics() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, null, Collections.emptyList());

        handler.execute();

        assertThat(registry.find("xxl.job.submitted").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("xxl.job.completed").counter().count()).isEqualTo(1.0);
    }

    @Test
    void initWithNullInitMethodShouldNotFail() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, null, Collections.emptyList());
        handler.init();
    }

    @Test
    void initWithMethodShouldInvoke() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        Method initMethod = getClass().getMethod("initMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, initMethod, null, Collections.emptyList());
        handler.init();
    }

    @Test
    void destroyWithNullDestroyMethodShouldNotFail() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, null, Collections.emptyList());
        handler.destroy();
    }

    @Test
    void destroyWithMethodShouldInvoke() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        Method destroyMethod = getClass().getMethod("destroyMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, destroyMethod, Collections.emptyList());
        handler.destroy();
    }

    @Test
    void toStringShouldContainTargetInfo() throws Exception {
        Method method = getClass().getMethod("sampleJobMethod");
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, this, method, null, null, Collections.emptyList());
        String str = handler.toString();
        assertThat(str).contains("sampleJobMethod");
    }
}
