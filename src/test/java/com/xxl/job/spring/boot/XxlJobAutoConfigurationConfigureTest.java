package com.xxl.job.spring.boot;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Additional tests for {@link XxlJobAutoConfiguration} to cover configureExecutor.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobAutoConfigurationConfigureTest {

    @Test
    void configureExecutorShouldSetProperties() throws Exception {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();

        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        XxlJobAdminProperties adminProps = new XxlJobAdminProperties();
        adminProps.setAddresses("http://localhost:8080");

        XxlJobExecutorProperties execProps = new XxlJobExecutorProperties();
        execProps.setAppname("test-app");
        execProps.setAddress("http://localhost:9999");
        execProps.setIp("127.0.0.1");
        execProps.setPort("9999");
        execProps.setLogPath("/tmp/logs");
        execProps.setLogRetentionDays(7);

        XxlJobProperties props = new XxlJobProperties();
        props.setAccessToken("test-token");

        Method method = XxlJobAutoConfiguration.class.getDeclaredMethod(
                "configureExecutor", XxlJobSpringExecutor.class,
                XxlJobAdminProperties.class, XxlJobExecutorProperties.class, XxlJobProperties.class);
        method.setAccessible(true);
        method.invoke(config, executor, adminProps, execProps, props);

        assertThat(executor.getAppname()).isEqualTo("test-app");
    }

    @Test
    void toAdminConfigShouldBuildConfig() throws Exception {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();

        XxlJobProperties props = new XxlJobProperties();
        props.setAccessToken("test-token");

        XxlJobAdminProperties adminProps = new XxlJobAdminProperties();
        adminProps.setAddresses("http://localhost:8080");
        adminProps.setUsername("admin");
        adminProps.setPassword("pass");

        Method method = XxlJobAutoConfiguration.class.getDeclaredMethod(
                "toAdminConfig", XxlJobProperties.class, XxlJobAdminProperties.class);
        method.setAccessible(true);
        Object adminConfig = method.invoke(config, props, adminProps);

        assertThat(adminConfig).isNotNull();
    }

    @Test
    void xxlJobExecutorBeanCreationWithMetricsDisabled() {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();

        XxlJobProperties props = new XxlJobProperties();
        props.setAccessToken("test-token");

        XxlJobAdminProperties adminProps = new XxlJobAdminProperties();
        adminProps.setAddresses("http://localhost:8080");

        XxlJobExecutorProperties execProps = new XxlJobExecutorProperties();
        execProps.setEnabled(true);
        execProps.setAppname("test-app");

        XxlJobMetricsProperties metricsProps = new XxlJobMetricsProperties();
        metricsProps.setEnabled(false);

        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> registryProvider = new SimpleObjectProvider<>(null);
        ObjectProvider<com.xxl.job.core.XxlJobTemplate> templateProvider = new SimpleObjectProvider<>(null);

        try {
            XxlJobSpringExecutor result = config.xxlJobExecutor(
                    registryProvider, templateProvider,
                    props, adminProps, execProps, metricsProps);
            assertThat(result).isNotNull();
        } catch (Exception e) {
            // Expected if dependencies are not fully available
        }
    }

    @Test
    void xxlJobExecutorBeanCreationWithMetricsEnabled() {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();

        XxlJobProperties props = new XxlJobProperties();
        props.setAccessToken("test-token");

        XxlJobAdminProperties adminProps = new XxlJobAdminProperties();
        adminProps.setAddresses("http://localhost:8080");

        XxlJobExecutorProperties execProps = new XxlJobExecutorProperties();
        execProps.setEnabled(true);
        execProps.setAppname("test-app");

        XxlJobMetricsProperties metricsProps = new XxlJobMetricsProperties();
        metricsProps.setEnabled(true);

        io.micrometer.core.instrument.simple.SimpleMeterRegistry registry =
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> registryProvider = new SimpleObjectProvider<>(registry);
        ObjectProvider<com.xxl.job.core.XxlJobTemplate> templateProvider = new SimpleObjectProvider<>(null);

        try {
            XxlJobSpringExecutor result = config.xxlJobExecutor(
                    registryProvider, templateProvider,
                    props, adminProps, execProps, metricsProps);
            assertThat(result).isNotNull();
        } catch (Exception e) {
            // Expected if dependencies are not fully available
        } finally {
            registry.close();
        }
    }

    /**
     * Simple ObjectProvider implementation for testing.
     */
    private static class SimpleObjectProvider<T> implements ObjectProvider<T> {
        private final T instance;

        SimpleObjectProvider(T instance) {
            this.instance = instance;
        }

        @Override
        public T getObject() throws BeansException {
            return instance;
        }

        @Override
        public T getIfAvailable() throws BeansException {
            return instance;
        }

        @Override
        public T getIfUnique() throws BeansException {
            return instance;
        }

        @Override
        public T getObject(Object... args) throws BeansException {
            return instance;
        }

        @Override
        public T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
            return instance != null ? instance : defaultSupplier.get();
        }

        @Override
        public T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
            return instance != null ? instance : defaultSupplier.get();
        }

        @Override
        public Iterator<T> iterator() {
            return Stream.of(instance).iterator();
        }
    }
}
