/*
 * Copyright (c) 2017, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.xxl.job.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;

@ConfigurationProperties(XxlJobAdminProperties.PREFIX)
@Data
public class XxlJobAdminProperties {

	public static final String PREFIX = "xxl.job.admin";

	/**
	 * 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
	 */
	private String addresses;

	/**
	 * 调度中心登录账号
	 */
	private String username;

	/**
	 * 调度中心登录密码
	 */
	private String password;

	/**
	 * 调度中心登录状态保持，开启后xxl-job登录状态不过期，默认：2H
	 */
	private boolean remember;

	/**
	 * 调度中心登录状态Cookie缓存配置
	 */
	private XxlJobAdminCookieProperties cookieCache;

	@Data
	public static class XxlJobAdminCookieProperties {

		/**
		 * he maximum size of the cache
		 */
		private long maximumSize = 10_000;
		/**
		 * the length of time after an entry is created that it should be automatically removed
		 */
		private Duration expireAfterWrite = Duration.ofMinutes(5);
		/**
		 * the length of time after an entry is created that it should be considered stale, and thus eligible for refresh
		 */
		private Duration refreshAfterWrite = Duration.ofMinutes(1);

	}


}
