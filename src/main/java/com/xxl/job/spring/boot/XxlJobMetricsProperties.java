package com.xxl.job.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	/**
	 * Whether include host tag.
	 */
	boolean includeHostTag;

	/**
	 * The tag keys to request.
	 */
	List<String> requestTagKeys;



}