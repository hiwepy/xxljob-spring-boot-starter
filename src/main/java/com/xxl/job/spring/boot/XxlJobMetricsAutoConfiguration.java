package com.xxl.job.spring.boot;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.spring.boot.metrics.XxlJobMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
@EnableConfigurationProperties({ XxlJobProperties.class, XxlJobAdminProperties.class, XxlJobExecutorProperties.class, XxlJobMetricsProperties.class })
@Slf4j
public class XxlJobMetricsAutoConfiguration {

	@Bean
	public XxlJobSpringExecutor xxlJobExecutor(
			ObjectProvider<MeterRegistry> registryProvider,
			ObjectProvider<XxlJobTemplate> xxlJobTemplateProvider,
			XxlJobProperties properties,
			XxlJobAdminProperties adminProperties,
			XxlJobExecutorProperties executorProperties,
			XxlJobMetricsProperties metricsProperties	) {

		log.info(">>>>>>>>>>> xxl-job auto binding and metrics executor init.");

		Collection<Tag> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? new ArrayList<>() : metricsProperties.getExtraTags()
				.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
		extraTags.add(Tag.of("executor", executorProperties.getAppname()));

		XxlJobAutoBindingAndMetricsSpringExecutor xxlJobExecutor = new XxlJobAutoBindingAndMetricsSpringExecutor(registryProvider.getObject(), xxlJobTemplateProvider.getObject(), extraTags);
		xxlJobExecutor.setAdminAddresses(adminProperties.getAddresses());
		xxlJobExecutor.setAppname(executorProperties.getAppname());
		xxlJobExecutor.setAppTitle(executorProperties.getTitle());
		xxlJobExecutor.setIp(executorProperties.getIp());
		xxlJobExecutor.setPort(Integer.parseInt(executorProperties.getPort()));
		xxlJobExecutor.setAccessToken(properties.getAccessToken());
		xxlJobExecutor.setLogPath(executorProperties.getLogpath());
		xxlJobExecutor.setLogRetentionDays(executorProperties.getLogretentiondays());
		return xxlJobExecutor;
	}

	@Bean
	public XxlJobMetrics xxlJobMetrics(XxlJobSpringExecutor executor) {
		return new XxlJobMetrics(executor);
	}

}
