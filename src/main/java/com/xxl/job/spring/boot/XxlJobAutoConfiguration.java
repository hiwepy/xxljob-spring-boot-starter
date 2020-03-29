package com.xxl.job.spring.boot;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.xxl.job.core.executor.XxlJobExecutor;

@Configuration
@ConditionalOnClass(XxlJobExecutor.class)
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobAutoConfiguration {

	@Bean
	public XxlJobTemplate xxlJobTemplate(RestTemplate restTemplate, XxlJobProperties properties) throws Exception {
		return new XxlJobTemplate(restTemplate, properties);
	}
	
	@PostConstruct
	public void init() {
	}
	
}
