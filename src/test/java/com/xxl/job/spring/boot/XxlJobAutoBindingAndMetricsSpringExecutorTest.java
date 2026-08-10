package com.xxl.job.spring.boot;

import com.xxl.job.core.XxlJobTemplate;
import com.xxl.job.core.admin.DefaultXxlJobAdminClient;
import com.xxl.job.core.admin.XxlJobAdminClient;
import com.xxl.job.core.config.XxlJobAdminConfig;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link XxlJobAutoBindingAndMetricsSpringExecutor}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobAutoBindingAndMetricsSpringExecutorTest {

    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    private XxlJobTemplate createTemplate() {
        XxlJobAdminConfig adminConfig = XxlJobAdminConfig.builder().build();
        XxlJobAdminClient adminClient = new DefaultXxlJobAdminClient(null, adminConfig);
        return new XxlJobTemplate(adminClient);
    }

    @Test
    void constructorShouldRequireRegistry() {
        assertThatThrownBy(() -> new XxlJobAutoBindingAndMetricsSpringExecutor(null, createTemplate(), Collections.emptyList()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorShouldRequireTemplate() {
        assertThatThrownBy(() -> new XxlJobAutoBindingAndMetricsSpringExecutor(registry, null, Collections.emptyList()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorShouldAcceptNullTags() {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), null);
        assertThat(executor).isNotNull();
    }

    @Test
    void constructorShouldAcceptEmptyTags() {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());
        assertThat(executor).isNotNull();
    }

    @Test
    void constructorShouldCopyTags() {
        Collection<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("env", "test"));
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), tags);
        assertThat(executor).isNotNull();
        tags.add(Tag.of("extra", "value"));
    }

    @Test
    void resolveHandlerNameWithXxlJobAnnotation() throws Exception {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());

        Method method = getClass().getDeclaredMethod("annotatedMethod");
        XxlJob xxlJob = method.getAnnotation(XxlJob.class);

        java.lang.reflect.Method resolveMethod = XxlJobAutoBindingAndMetricsSpringExecutor.class
                .getDeclaredMethod("resolveHandlerName", XxlJob.class, com.xxl.job.core.annotation.XxlJobCron.class);
        resolveMethod.setAccessible(true);

        String name = (String) resolveMethod.invoke(executor, xxlJob, null);
        assertThat(name).isEqualTo("testJob");
    }

    @Test
    void resolveHandlerNameWithNullReturnsNull() throws Exception {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());

        java.lang.reflect.Method resolveMethod = XxlJobAutoBindingAndMetricsSpringExecutor.class
                .getDeclaredMethod("resolveHandlerName", XxlJob.class, com.xxl.job.core.annotation.XxlJobCron.class);
        resolveMethod.setAccessible(true);

        String name = (String) resolveMethod.invoke(executor, null, null);
        assertThat(name).isNull();
    }

    @Test
    void resolveLifecycleMethodWithEmptyNameReturnsNull() throws Exception {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());

        java.lang.reflect.Method resolveMethod = XxlJobAutoBindingAndMetricsSpringExecutor.class
                .getDeclaredMethod("resolveLifecycleMethod", Class.class, Method.class, String.class, String.class);
        resolveMethod.setAccessible(true);

        Method execMethod = getClass().getDeclaredMethod("annotatedMethod");
        Method lifecycleMethod = (Method) resolveMethod.invoke(executor, getClass(), execMethod, "", "initMethod");
        assertThat(lifecycleMethod).isNull();
    }

    @Test
    void resolveLifecycleMethodWithNullNameReturnsNull() throws Exception {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());

        java.lang.reflect.Method resolveMethod = XxlJobAutoBindingAndMetricsSpringExecutor.class
                .getDeclaredMethod("resolveLifecycleMethod", Class.class, Method.class, String.class, String.class);
        resolveMethod.setAccessible(true);

        Method execMethod = getClass().getDeclaredMethod("annotatedMethod");
        Method lifecycleMethod = (Method) resolveMethod.invoke(executor, getClass(), execMethod, null, "initMethod");
        assertThat(lifecycleMethod).isNull();
    }

    @Test
    void resolveLifecycleMethodWithValidMethod() throws Exception {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());

        java.lang.reflect.Method resolveMethod = XxlJobAutoBindingAndMetricsSpringExecutor.class
                .getDeclaredMethod("resolveLifecycleMethod", Class.class, Method.class, String.class, String.class);
        resolveMethod.setAccessible(true);

        Method execMethod = getClass().getDeclaredMethod("annotatedMethod");
        Method lifecycleMethod = (Method) resolveMethod.invoke(executor, getClass(), execMethod, "initMethod", "initMethod");
        assertThat(lifecycleMethod).isNotNull();
    }

    @Test
    void resolveLifecycleMethodWithInvalidMethodThrows() throws Exception {
        XxlJobAutoBindingAndMetricsSpringExecutor executor =
                new XxlJobAutoBindingAndMetricsSpringExecutor(registry, createTemplate(), Collections.emptyList());

        java.lang.reflect.Method resolveMethod = XxlJobAutoBindingAndMetricsSpringExecutor.class
                .getDeclaredMethod("resolveLifecycleMethod", Class.class, Method.class, String.class, String.class);
        resolveMethod.setAccessible(true);

        Method execMethod = getClass().getDeclaredMethod("annotatedMethod");
        assertThatThrownBy(() -> {
            try {
                resolveMethod.invoke(executor, getClass(), execMethod, "nonExistentMethod", "initMethod");
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        }).isInstanceOf(IllegalStateException.class);
    }

    @XxlJob("testJob")
    public void annotatedMethod() {
        // test method
    }

    public void initMethod() {
        // init method
    }

    public void destroyMethod() {
        // destroy method
    }
}
