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

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XxlJobInfo {
	
	private int id;
	private int jobGroup;
	private String jobCron;
	private String jobDesc;
	private Date addTime;
	private Date updateTime;
	private String author;
	private String alarmEmail;
	private String executorRouteStrategy;
	private String executorHandler;
	private String executorParam;
	private String executorBlockStrategy;
	private String executorFailStrategy;
	private int executorTimeout;
	private String glueType;
	private String glueSource;
	private String glueRemark;
	private Date glueUpdatetime;
	private String childJobId;
	private String jobStatus;
	private String appName;
	private Integer bizType;
	private String bizCode;
	
}
