package com.xxl.job.spring.boot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobExecutorProperties}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class XxlJobExecutorPropertiesTest {

    @Test
    void defaultValues() {
        XxlJobExecutorProperties props = new XxlJobExecutorProperties();
        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getAppname()).isEmpty();
        assertThat(props.getTitle()).isEmpty();
        assertThat(props.getAddress()).isNull();
        assertThat(props.getIp()).isEmpty();
        assertThat(props.getPort()).isEqualTo("-1");
        assertThat(props.getLogPath()).isEqualTo("/data/applogs/xxl-job/jobhandler");
        assertThat(props.getLogRetentionDays()).isEqualTo(30);
    }

    @Test
    void prefix() {
        assertThat(XxlJobExecutorProperties.PREFIX).isEqualTo("xxl.job.executor");
    }

    @Test
    void settersAndGetters() {
        XxlJobExecutorProperties props = new XxlJobExecutorProperties();
        props.setEnabled(true);
        props.setAppname("test-app");
        props.setTitle("Test Title");
        props.setAddress("http://localhost:8080");
        props.setIp("127.0.0.1");
        props.setPort("9999");
        props.setLogPath("/tmp/logs");
        props.setLogRetentionDays(7);

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getAppname()).isEqualTo("test-app");
        assertThat(props.getTitle()).isEqualTo("Test Title");
        assertThat(props.getAddress()).isEqualTo("http://localhost:8080");
        assertThat(props.getIp()).isEqualTo("127.0.0.1");
        assertThat(props.getPort()).isEqualTo("9999");
        assertThat(props.getLogPath()).isEqualTo("/tmp/logs");
        assertThat(props.getLogRetentionDays()).isEqualTo(7);
    }
}
