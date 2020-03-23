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
package com.xxl.job.spring.boot.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JobBaseModel {

	/**
	 * id
	 */
	private int id;

	/**
	 * jobGroup
	 */
	private int jobGroup;

	/**
	 * 定时cron表达式
	 */
	private String jobCron;

	/**
	 * 任务描述
	 */
	private String jobDesc;

	/**
	 * 负责人
	 */
	private String author;

	/**
	 * 执行策略
	 */
	private String executorRouteStrategy;

	/**
	 * 执行器
	 */
	private String executorHandler;

	/**
	 * 失败超时时间
	 */
	private int executorTimeout;

	/**
	 * 失败重试次数
	 */
	private int executorFailRetryCount;

	/**
	 * 运行模式
	 */
	private String glueType;

	/**
	 * 启动状态(0.未运行--1.运行)
	 */
	private int triggerStatus;

}
