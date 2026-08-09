package com.xxl.job.spring.boot;

import com.xxl.job.core.AdminVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XxlJobAdminProperties}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class XxlJobAdminPropertiesTest {

    @Test
    void defaultValues() {
        XxlJobAdminProperties props = new XxlJobAdminProperties();
        assertThat(props.getAddresses()).isNull();
        assertThat(props.getUsername()).isNull();
        assertThat(props.getPassword()).isNull();
        assertThat(props.isRemember()).isFalse();
        assertThat(props.getVersion()).isEqualTo(AdminVersion.V2_X);
    }

    @Test
    void prefix() {
        assertThat(XxlJobAdminProperties.PREFIX).isEqualTo("xxl.job.admin");
    }

    @Test
    void settersAndGetters() {
        XxlJobAdminProperties props = new XxlJobAdminProperties();
        props.setAddresses("http://localhost:8080/xxl-job-admin");
        props.setUsername("admin");
        props.setPassword("password123");
        props.setRemember(true);
        props.setVersion(AdminVersion.V3_X);

        assertThat(props.getAddresses()).isEqualTo("http://localhost:8080/xxl-job-admin");
        assertThat(props.getUsername()).isEqualTo("admin");
        assertThat(props.getPassword()).isEqualTo("password123");
        assertThat(props.isRemember()).isTrue();
        assertThat(props.getVersion()).isEqualTo(AdminVersion.V3_X);
    }
}
