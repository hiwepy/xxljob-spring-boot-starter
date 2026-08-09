package com.xxl.job.spring.boot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobAutoConfiguration}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class XxlJobAutoConfigurationTest {

    @Test
    void resolvePortWithNullReturnsNegativeOne() throws Exception {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();
        // Use reflection to test private method
        java.lang.reflect.Method method = XxlJobAutoConfiguration.class.getDeclaredMethod("resolvePort", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(config, (String) null);
        assertThat(result).isEqualTo(-1);
    }

    @Test
    void resolvePortWithEmptyReturnsNegativeOne() throws Exception {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();
        java.lang.reflect.Method method = XxlJobAutoConfiguration.class.getDeclaredMethod("resolvePort", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(config, "");
        assertThat(result).isEqualTo(-1);
    }

    @Test
    void resolvePortWithValidPortReturnsParsed() throws Exception {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();
        java.lang.reflect.Method method = XxlJobAutoConfiguration.class.getDeclaredMethod("resolvePort", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(config, "9999");
        assertThat(result).isEqualTo(9999);
    }

    @Test
    void createTrustAllSslContextReturnsNonNull() throws Exception {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();
        java.lang.reflect.Method method = XxlJobAutoConfiguration.class.getDeclaredMethod("createTrustAllSslContext");
        method.setAccessible(true);
        javax.net.ssl.SSLContext ctx = (javax.net.ssl.SSLContext) method.invoke(config);
        assertThat(ctx).isNotNull();
    }

    @Test
    void propertiesBeansCreated() {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();
        assertThat(config.xxlJobProperties()).isNotNull();
        assertThat(config.xxlJobAdminProperties()).isNotNull();
        assertThat(config.xxlJobExecutorProperties()).isNotNull();
    }

    @Test
    void unirestInstanceCreated() {
        XxlJobAutoConfiguration config = new XxlJobAutoConfiguration();
        Object instance = config.unirestInstance();
        assertThat(instance).isNotNull();
    }
}
