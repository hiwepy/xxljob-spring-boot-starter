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
package com.xxl.job.spring.boot.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class XxlJobInfo implements Serializable {

	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 执行器主键ID
	 */
	private Integer jobGroup;

	/**
	 * 任务描述
	 */
	private String jobDesc;

	/**
	 * 负责人
	 */
	private String author;

	/**
	 * 报警邮件
	 */
	private String alarmEmail;

	/**
	 * 调度类型 ScheduleTypeEnum
	 */
	private String scheduleType;

	/**
	 * 调度配置，值含义取决于调度类型
	 */
	private String scheduleConf;
	private String cronExpression;

	/**
	 * 调度过期策略
	 */
	private String misfireStrategy;

	/**
	 * 执行器路由策略
	 */
	private String executorRouteStrategy;

	/**
	 * 执行器，任务Handler名称
	 */
	private String executorHandler;

	/**
	 * 执行器，任务参数
	 */
	private String executorParam;

	/**
	 * 阻塞处理策略
	 */
	private String executorBlockStrategy;

	/**
	 * 任务执行超时时间，单位秒
	 */
	private int executorTimeout;

	/**
	 * 失败重试次数
	 */
	private int executorFailRetryCount;

	/**
	 * GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
	 */
	private String glueType;

	/**
	 * GLUE源代码
	 */
	private String glueSource;

	/**
	 * GLUE备注
	 */
	private String glueRemark;

	/**
	 * GLUE更新时间
	 */
	private Date glueUpdatetime;

	/**
	 * 子任务ID，多个逗号分隔
	 */
	private String childJobId;

	/**
	 * 执行器，创建时间
	 */
	private Date addTime;

	/**
	 * 执行器，更新时间
	 */
	private Date updateTime;

	/**
	 * 调度状态：0-停止，1-运行
	 */
	private int triggerStatus;

	/**
	 * 上次调度时间
	 */
	private long triggerLastTime;

	/**
	 * 下次调度时间
	 */
	private long triggerNextTime;



	
}
