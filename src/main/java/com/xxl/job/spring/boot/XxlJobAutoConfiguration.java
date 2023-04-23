package com.xxl.job.spring.boot;

import javax.annotation.PostConstruct;

import com.xxl.job.spring.boot.cache.CaffeineCacheCookieJar;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Configuration
@ConditionalOnClass(XxlJobExecutor.class)
@ConditionalOnProperty(prefix = XxlJobProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({
	XxlJobAdminProperties.class,
	XxlJobAdminCookieProperties.class,
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
	public XxlJobTemplate xxlJobTemplate(
			ObjectProvider<OkHttpClient> okhttp3ClientProvider,
			ObjectProvider<CookieJar> cookieJarProvider,
			XxlJobProperties properties,
			XxlJobAdminProperties adminProperties,
			XxlJobAdminCookieProperties cookieProperties,
			XxlJobExecutorProperties executorProperties) throws Exception {
		OkHttpClient okhttp3Client = okhttp3ClientProvider.getIfAvailable(() -> new OkHttpClient.Builder()
				.cookieJar(new CaffeineCacheCookieJar(cookieProperties.getMaximumSize(), cookieProperties.getExpireAfterWrite(), cookieProperties.getExpireAfterAccess())).build());
		return new XxlJobTemplate(okhttp3Client, properties, adminProperties, executorProperties);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public XxlJobSpringExecutor xxlJobExecutor(
			XxlJobTemplate xxlJobTemplate,
			XxlJobProperties properties, 
			XxlJobAdminProperties adminProperties,
			XxlJobExecutorProperties executorProperties) {
		logger.info(">>>>>>>>>>> xxl-job executor init.");
		XxlJobSpringExecutorWhitRegister xxlJobExecutor = new XxlJobSpringExecutorWhitRegister(xxlJobTemplate);
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
	
	 
	@PostConstruct
	public void init() {
	}
	
}
