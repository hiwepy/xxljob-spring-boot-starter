package com.xxl.job.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Configuration properties.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@ConfigurationProperties(XxlJobMetricsProperties.PREFIX)
@Data
public class XxlJobMetricsProperties {

	public static final String PREFIX = "xxl.job.metrics";

	/**
	 * Whether Enable Xxl Job Metrics.
	 */
	private boolean enabled = false;

	/**
	 * Extra tags for metrics.
	 */
	private Map<String, String > extraTags = new LinkedHashMap<>(16);

}
