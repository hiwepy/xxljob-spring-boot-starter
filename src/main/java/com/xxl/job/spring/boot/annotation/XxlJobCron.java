/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
package com.xxl.job.spring.boot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.spring.boot.executor.ExecutorRouteStrategyEnum;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJobCron {

	/*
	 * 任务执行CRON表达式
	 */
	String cron() default "";

	/*
	 * 负责人
	 */
	String author() default "xxl-job";

	/*
	 * 报警邮件
	 */
	String alarmEmail() default "";

	/*
	 * 执行器描述
	 */
	String desc() default "";

	/*
	 * 执行器，任务参数
	 */
	String param() default "";

	/*
	 * 失败重试次数
	 */
	int failRetryCount() default 3;

	/*
	 * 阻塞处理策略
	 */
	ExecutorBlockStrategyEnum blockStrategy() default ExecutorBlockStrategyEnum.COVER_EARLY;

	/*
	 * 执行器路由策略
	 */
	ExecutorRouteStrategyEnum routeStrategy() default ExecutorRouteStrategyEnum.LEAST_FREQUENTLY_USED;

	/*
	 * 任务执行超时时间，单位秒
	 */
	int timeout() default 3000;

}
