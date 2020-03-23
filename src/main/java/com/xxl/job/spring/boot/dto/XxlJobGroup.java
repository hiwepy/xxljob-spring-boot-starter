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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XxlJobGroup {

	private int id;

	private String appName;

	private String title;

	private int order;

	/**
	 * 执行器地址类型：0=自动注册、1=手动录入
	 */
	private int addressType;

	/**
	 * 执行器地址列表，多地址逗号分隔(手动录入)
	 */
	private String addressList;

	/**
	 * 执行器地址列表(系统注册)
	 */
	private List<String> registryList;

	public List<String> getRegistryList() {
		if (addressList != null && addressList.trim().length() > 0) {
			registryList = new ArrayList<String>(Arrays.asList(addressList.split(",")));
		}
		return registryList;
	}

}
