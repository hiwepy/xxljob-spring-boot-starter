package com.xxl.job.spring.boot;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.spring.boot.metrics.XxlJobMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * XXL-Job Micrometer 指标自动配置。
 * <p>
 * Spring Boot 4.0 移除了 actuate.autoconfigure.metrics.MetricsAutoConfiguration，
 * 因此不再使用 @AutoConfigureAfter 引用该类。
 * </p>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ MeterRegistry.class, XxlJobExecutor.class })
@ConditionalOnProperty(prefix = XxlJobMetricsProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({ XxlJobMetricsProperties.class })
@Slf4j
public class XxlJobMetricsAutoConfiguration {

	@Bean
	@Lazy
	@ConditionalOnProperty(prefix = XxlJobExecutorProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
	/**
	 * XXL Job Metrics.
	 *
	 * @param executor the executor
	 * @return the result
	 */
	public XxlJobMetrics xxlJobMetrics(@Lazy XxlJobSpringExecutor executor) {
		return new XxlJobMetrics(executor);
	}

}
