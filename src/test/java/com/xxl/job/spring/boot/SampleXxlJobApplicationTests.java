package com.xxl.job.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SampleXxlJobApplicationTests {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	@Bean
	public FormHttpMessageConverter formHttpMessageConverter() {
		return new FormHttpMessageConverter();
	}


	public static void main(String[] args) {
		SpringApplication.run(SampleXxlJobApplicationTests.class,args);
	}

}