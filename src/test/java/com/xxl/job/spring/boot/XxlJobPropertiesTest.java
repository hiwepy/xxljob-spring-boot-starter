package com.xxl.job.spring.boot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobPropertiesTest {

    @Test
    void defaultValues() {
        XxlJobProperties props = new XxlJobProperties();
        assertThat(props.getAccessToken()).isNull();
    }

    @Test
    void prefix() {
        assertThat(XxlJobProperties.PREFIX).isEqualTo("xxl.job");
    }

    @Test
    void settersAndGetters() {
        XxlJobProperties props = new XxlJobProperties();
        props.setAccessToken("test-token");
        assertThat(props.getAccessToken()).isEqualTo("test-token");
    }
}
