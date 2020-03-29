package com.xxl.job.spring.boot;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

@Configuration
@ConditionalOnClass(XxlJobExecutor.class)
@EnableConfigurationProperties({
	XxlJobAdminProperties.class,
	XxlJobProperties.class,
	XxlJobExecutorProperties.class
})
public class XxlJobAutoConfiguration {

	private Logger logger = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

	@Bean
	public XxlJobTemplate xxlJobTemplate(RestTemplate restTemplate, XxlJobProperties properties) throws Exception {
		return new XxlJobTemplate(restTemplate, properties);
	}
	
	@Bean(initMethod = "start", destroyMethod = "destroy")
	@ConditionalOnMissingBean
	public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties properties, XxlJobAdminProperties adminProperties,
			XxlJobExecutorProperties executorProperties) {
		logger.info(">>>>>>>>>>> xxl-job config init.");
		XxlJobSpringExecutor xxlJobExecutor = new XxlJobSpringExecutor();
		xxlJobExecutor.setAdminAddresses(adminProperties.getAddresses());
		xxlJobExecutor.setAppName(executorProperties.getAppname());
		xxlJobExecutor.setIp(executorProperties.getIp());
		xxlJobExecutor.setPort(executorProperties.getPort());
		xxlJobExecutor.setAccessToken(properties.getAccessToken());
		xxlJobExecutor.setLogPath(executorProperties.getLogpath());
		xxlJobExecutor.setLogRetentionDays(executorProperties.getLogretentiondays());
		return xxlJobExecutor;
	}
	 
	@PostConstruct
	public void init() {
	}
	
}
