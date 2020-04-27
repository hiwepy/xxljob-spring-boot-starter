package com.xxl.job.spring.boot;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

@Configuration
@ConditionalOnClass(XxlJobExecutor.class)
@ConditionalOnProperty(prefix = XxlJobProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({
	XxlJobAdminProperties.class,
	XxlJobProperties.class,
	XxlJobExecutorProperties.class
})
public class XxlJobAutoConfiguration {

	private Logger logger = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public RestTemplate restTemplate() throws Exception {
		return new RestTemplateBuilder().build();
	}
	
	@Bean
	public XxlJobTemplate xxlJobTemplate(RestTemplate restTemplate, XxlJobProperties properties,
			XxlJobAdminProperties adminProperties, 
			XxlJobExecutorProperties executorProperties) throws Exception {
		return new XxlJobTemplate(restTemplate, properties, adminProperties, executorProperties);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public XxlJobSpringExecutor xxlJobExecutor(
			XxlJobTemplate xxlJobTemplate,
			XxlJobProperties properties, 
			XxlJobAdminProperties adminProperties,
			XxlJobExecutorProperties executorProperties) {
		logger.info(">>>>>>>>>>> xxl-job config init.");
		XxlJobSpringExecutorWhitRegister xxlJobExecutor = new XxlJobSpringExecutorWhitRegister(xxlJobTemplate);
		xxlJobExecutor.setAdminAddresses(adminProperties.getAddresses());
		xxlJobExecutor.setAppName(executorProperties.getAppname());
		xxlJobExecutor.setIp(executorProperties.getIp());
		xxlJobExecutor.setPort(Integer.parseInt(executorProperties.getPort()));
		xxlJobExecutor.setAccessToken(properties.getAccessToken());
		xxlJobExecutor.setLogPath(executorProperties.getLogpath());
		xxlJobExecutor.setLogRetentionDays(executorProperties.getLogretentiondays());
		return xxlJobExecutor;
	}
	
	 
	@PostConstruct
	public void init() {
	}
	
}
