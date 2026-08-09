package com.xxl.job.spring.boot.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MetricNames}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class MetricNamesTest {

    @Test
    void nameWithSinglePart() {
        assertThat(MetricNames.name("foo")).isEqualTo("foo");
    }

    @Test
    void nameWithMultipleParts() {
        assertThat(MetricNames.name("foo", "bar", "baz")).isEqualTo("foo.bar.baz");
    }

    @Test
    void nameSkipsEmptyParts() {
        assertThat(MetricNames.name("foo", "", "bar", null, "baz")).isEqualTo("foo.bar.baz");
    }

    @Test
    void nameWithClassType() {
        assertThat(MetricNames.name(String.class, "bar")).isEqualTo("java.lang.String.bar");
    }

    @Test
    void nameWithNullTypeThrows() {
        assertThatThrownBy(() -> MetricNames.name((Class<?>) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nameWithOnlyEmptyParts() {
        assertThat(MetricNames.name("", null, "")).isEmpty();
    }
}
