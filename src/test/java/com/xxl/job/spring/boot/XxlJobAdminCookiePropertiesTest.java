package com.xxl.job.spring.boot;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobAdminCookieProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class XxlJobAdminCookiePropertiesTest {

    @Test
    void defaultValues() {
        XxlJobAdminCookieProperties props = new XxlJobAdminCookieProperties();
        assertThat(props.getMaximumSize()).isEqualTo(10_000L);
        assertThat(props.getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(30));
        assertThat(props.getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void prefix() {
        assertThat(XxlJobAdminCookieProperties.PREFIX).isEqualTo("xxl.job.admin.cookie");
    }

    @Test
    void settersAndGetters() {
        XxlJobAdminCookieProperties props = new XxlJobAdminCookieProperties();
        props.setMaximumSize(5000);
        props.setExpireAfterWrite(Duration.ofMinutes(10));
        props.setExpireAfterAccess(Duration.ofMinutes(5));

        assertThat(props.getMaximumSize()).isEqualTo(5000);
        assertThat(props.getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(10));
        assertThat(props.getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(5));
    }
}
