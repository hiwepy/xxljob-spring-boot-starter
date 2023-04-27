package com.xxl.job.spring.boot;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.spring.boot.aspect.XxlJobExecutorAspect;
import com.xxl.job.spring.boot.metrics.XxlJobMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * XXL Job
 */
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ MeterRegistry.class, XxlJobExecutor.class  })
@ConditionalOnProperty(prefix = XxlJobMetricsProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({ XxlJobMetricsProperties.class, XxlJobExecutorProperties.class })
public class XxlJobMetricsAutoConfiguration {

	@Bean
	public XxlJobExecutorAspect xxlJobExecutorAspect(ObjectProvider<MeterRegistry> registryProvider,
													 XxlJobExecutorProperties executorProperties,
													 XxlJobMetricsProperties metricsProperties	) {
		Collection<Tag> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? Collections.emptyList() : metricsProperties.getExtraTags()
				.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
		extraTags.add(Tag.of("executor", executorProperties.getAppname()));
		return new XxlJobExecutorAspect(registryProvider.getObject(), extraTags);
	}

	@Bean
	public XxlJobMetrics xxlJobMetrics(XxlJobSpringExecutor executor) {
		return new XxlJobMetrics(executor);
	}

}
