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
package com.xxl.job.spring.boot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.spring.boot.model.XxlJobGroup;
import com.xxl.job.spring.boot.model.XxlJobGroupList;
import com.xxl.job.spring.boot.model.XxlJobInfo;
import com.xxl.job.spring.boot.model.XxlJobInfoList;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class XxlJobTemplate {

	public final static String APPLICATION_JSON_VALUE = "application/json";
	public final static String APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8";
	public final static okhttp3.MediaType APPLICATION_JSON = okhttp3.MediaType.parse(APPLICATION_JSON_VALUE);
	public final static okhttp3.MediaType APPLICATION_JSON_UTF8 = okhttp3.MediaType.parse(APPLICATION_JSON_UTF8_VALUE);

	protected OkHttpClient okhttp3Client;
	protected XxlJobProperties properties;
	protected XxlJobAdminProperties adminProperties;
	protected XxlJobExecutorProperties executorProperties;

	public XxlJobTemplate( OkHttpClient okhttp3Client,
						   XxlJobProperties properties,
			XxlJobAdminProperties adminProperties, 
			XxlJobExecutorProperties executorProperties) {
		this.okhttp3Client = okhttp3Client;
		this.properties = properties;
		this.adminProperties = adminProperties;
		this.executorProperties = executorProperties;
	}

	public ReturnT<String> login(String userName, String password, boolean remember) {
		try {
			// xxl-job admin 请求参数
			Map<String, Object> paramMap = new HashMap<>(3);
			paramMap.put("userName", userName);
			paramMap.put("password", password);
			paramMap.put("ifRemember", remember ? "on" : "off");
			// xxl-job admin 请求体
			String url = this.joinPath(XxlJobConstants.LOGIN_GET);
			Request request = this.buildRequestEntity(url, paramMap, true);
			// xxl-job admin 请求操作
			Response response = okhttp3Client.newCall(request).execute();
			// xxl-job admin 请求结果成功
			if(response.isSuccessful()) {
				log.info("xxl-job login success.");
				// 从返回结果中获取cookie
				String cookie = response.header(HttpHeaders.SET_COOKIE);
				log.info("xxl-job cookie {}.", cookie);
				// 返回cookie
				return new ReturnT<String>(cookie);
			}
			// xxl-job admin 请求结果失败
			log.error("xxl-job login fail.");
			// xxl-job admin 请求结果失败
			return new ReturnT<String>(ReturnT.FAIL_CODE, response.toString());
		} catch (IOException e) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	private boolean isResponseJson(Response response) {
		String contentType = response.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		return contentType != null && (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
				|| contentType.startsWith(MediaType.APPLICATION_JSON_UTF8_VALUE));
	}

	/**
	 * 退出登录
	 * @return
	 */
	public ReturnT<String> logout() throws IOException {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = Collections.emptyMap();
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.LOGOUT_GET);
		Request request = this.buildRequestEntity(url, paramMap);
		// xxl-job admin 请求操作
		Response response = okhttp3Client.newCall(request).execute();
		// xxl-job admin 请求结果成功
		if(response.isSuccessful()) {
			log.info("xxl-job logout success.");
			// 返回cookie
			return ReturnT.SUCCESS;
		}
		// xxl-job admin 请求结果失败
		log.error("xxl-job logout fail.");
		// xxl-job admin 请求结果失败
		return new ReturnT<String>(ReturnT.FAIL_CODE, response.toString());
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start	起始位置
	 * @param length 数量
	 * @param appname 执行器名称
	 * @param title 执行器标题
	 * @return
	 */
	public ReturnT<XxlJobGroupList> jobInfoGroupList(int start, int length, String appname, String title) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(7);
		paramMap.put("start", Math.max(0, start));
		paramMap.put("length", Math.min(length, 5));
		paramMap.put("appname", appname);
		paramMap.put("title", title);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBGROUP_PAGELIST);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request, XxlJobGroupList.class);
	}

	/**
	 * 获取调度任务组
	 * @param jobGroupId 调度任务组ID
	 * @return ReturnT
	 */
	public ReturnT<XxlJobGroup> jobInfoGroup(Integer jobGroupId) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(1);
		paramMap.put("id", jobGroupId);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBGROUP_GET);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
	}

	/**
	 * 添加调度任务组
	 * @param jobGroup 调度任务组信息Model
	 * @return	ReturnT
	 */
	public ReturnT<String> addJobGroup(XxlJobGroup jobGroup) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(jobGroup), Map.class);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBGROUP_SAVE);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
	}

	/**
	 * 更新调度任务组
	 * @param jobGroup 调度任务组信息Model
	 * @return ReturnT
	 */
	public ReturnT<String> updateJobGroup(XxlJobGroup jobGroup) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(jobGroup), Map.class);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBGROUP_UPDATE);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
	}

	/**
	 * 删除调度任务组
	 * @param jobGroupId 调度任务组ID
	 * @return	ReturnT
	 */
	public ReturnT<String> removeJobGroup(Integer jobGroupId) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(1);
		paramMap.put("id", jobGroupId);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBGROUP_REMOVE);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start	起始位置
	 * @param length 数量
	 * @param jobGroup 执行器主键ID
	 * @return
	 */
	public ReturnT<XxlJobInfoList> jobInfoList(int start, int length, Integer jobGroup) {
		return this.jobInfoList(start, length, jobGroup, -1, null, null, null);
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start	起始位置
	 * @param length 数量
	 * @param jobGroup 执行器主键ID
	 * @param triggerStatus 调度状态：0-停止，1-运行
	 * @return
	 */
    public ReturnT<XxlJobInfoList> jobInfoList(int start, int length, Integer jobGroup, Integer triggerStatus) {
    	return this.jobInfoList(start, length, jobGroup, triggerStatus, "", "", "");
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start 起始位置
	 * @param length 数量
	 * @param jobGroup 执行器主键ID
	 * @param triggerStatus 调度状态：0-停止，1-运行
	 * @param jobDesc 任务描述
	 * @param executorHandler 执行器任务handler
	 * @param author 任务创建者
	 * @return
	 */
    public ReturnT<XxlJobInfoList> jobInfoList(int start, int length, Integer jobGroup,
											   Integer triggerStatus, String jobDesc, String executorHandler, String author) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(7);
		paramMap.put("start", Math.max(0, start));
		paramMap.put("length", Math.max(length, 5));
		paramMap.put("jobGroup", jobGroup);
		paramMap.put("triggerStatus", triggerStatus);
		paramMap.put("jobDesc", jobDesc);
		paramMap.put("executorHandler", executorHandler);
		paramMap.put("author", author);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_PAGELIST);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request, XxlJobInfoList.class);
	}

	/**
	 * 新增调度任务
	 * @param jobInfo 调用任务信息Model
	 * @return 任务id
	 * @return
	 */
	public ReturnT<Integer> addJob(XxlJobInfo jobInfo) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(jobInfo), Map.class);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_ADD);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
    }

	/**
	 * 修改调度任务
	 * @param jobInfo 调用任务信息Model
	 * @return
	 */
    public ReturnT<String> updateJob(XxlJobInfo jobInfo) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(jobInfo), Map.class);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_UPDATE);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
    }

	/**
	 * 删除调度任务
	 * @param jobId 任务id
	 * @return
	 */
    public ReturnT<String> removeJob(Integer jobId) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(1);
		paramMap.put("id", jobId);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_REMOVE);
		Request request = this.buildRequestEntity(url,paramMap, true);
		// xxl-job admin 请求操作
		return this.doRequest(request);
    }

	/**
	 * 停止调度
	 * @param jobId 任务id
	 * @return
	 */
    public ReturnT<String> stopJob(Integer jobId) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(1);
		paramMap.put("id", jobId);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_STOP);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
    }

	/**
	 * 开启调度
	 * @param jobId 任务id
	 * @return
	 */
	public ReturnT<String> startJob(Integer jobId) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(1);
		paramMap.put("id", jobId);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_START);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
    }

	/**
	 *
	 * 手动触发一次调度
	 * @param jobInfo 调用任务信息Model
	 * @return ReturnT
	 */
	public ReturnT<String> triggerJob(XxlJobInfo jobInfo) {
		return this.triggerJob(jobInfo.getId(), jobInfo.getExecutorParam());
	}

	/**
	 * 手动触发一次调度
	 * @param jobInfoId 调用任务ID
	 * @param executorParam 执行器参数
	 * @return ReturnT
	 */
    public ReturnT<String> triggerJob(Integer jobInfoId, String executorParam) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(2);
		paramMap.put("id", jobInfoId);
		paramMap.put("executorParam", executorParam);
		// xxl-job admin 请求体
		String url = this.joinPath(XxlJobConstants.JOBINFO_TRIGGER);
		Request request = this.buildRequestEntity(url, paramMap, false);
		// xxl-job admin 请求操作
		return this.doRequest(request);
    }

	private Request buildRequestEntity(String url, Map<String, Object> paramMap) {
		return this.buildRequestEntity(url, paramMap, false);
	}
	
	private Request buildRequestEntity(String url, Map<String, Object> paramMap, boolean isLoginRequest) {

		// xxl-job admin 请求头
		Headers.Builder headers = new Headers.Builder()
				.add(XxlJobConstants.XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());

		// xxl-job admin 请求体

		// 创建一个RequestBody(参数1：数据类型 参数2传递的json串)
		FormBody.Builder builder = new FormBody.Builder();
		for (String key : paramMap.keySet()) {
			Object obj = paramMap.get(key);
			if (obj != null) {
				builder.addEncoded(key, paramMap.get(key).toString());
			} else {
				builder.addEncoded(key, "");
			}
		}
		FormBody  requestBody = builder.build();

		// 创建一个请求对象
		HttpUrl httpUrl = HttpUrl.parse(url);
		Request.Builder request = new Request.Builder().url(httpUrl).headers(headers.build()).post(requestBody);

		// 非登录请求需要检查登录状态
		if(!isLoginRequest){
			this.loginIfNeed(httpUrl, headers, request);
		}
		return request.build();
	}

	private void loginIfNeed(HttpUrl httpUrl, Headers.Builder headers, Request.Builder request) {

		// xxl-job admin cookie
		CookieJar cookieJar  = okhttp3Client.cookieJar();
		List<Cookie> cookies = cookieJar.loadForRequest(httpUrl);
		// 缓存中的 cookie 不为空，查找我们需要的 cookie
		if(CollectionUtils.isEmpty(cookies) || cookies.stream().noneMatch(cookie -> XxlJobConstants.XXL_RPC_COOKIE.equals(cookie.name()))){
			// 缓存中的 cookie 为空，或者缓存中的 cookie 不包含我们需要的 cookie
			this.login(adminProperties.getUsername(), adminProperties.getPassword(), adminProperties.isRemember());
		}

	}

	private <T> ReturnT<T> doRequest(Request request, Class<T> objectClass) {
		// xxl-job admin 请求操作
		try {
			// 发送请求获取响应
			Response response = okhttp3Client.newCall(request).execute();
			// 请求结果处理
			// xxl-job admin 请求结果成功
			if(response.isSuccessful()) {
				log.info("xxl-job request successful.");
				String body = response.body().string();
				log.debug("xxl-job response body: {} .", body);
				T rt = JSON.parseObject(body, objectClass);
				return new ReturnT<T>(rt);
			}
			log.error("xxl-job request fail.");
			// xxl-job admin 请求结果失败
			return new ReturnT<T>(ReturnT.FAIL_CODE, response.toString());
		} catch (IOException e) {
			return new ReturnT<T>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	private <T> ReturnT<T> doRequest(Request request) {
		// xxl-job admin 请求操作
		try {
			// 发送请求获取响应
			Response response = okhttp3Client.newCall(request).execute();
			// 请求结果处理
			return this.parseResponseEntity(response);
		} catch (IOException e) {
			return new ReturnT<T>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	private <T> ReturnT<T> parseResponseEntity(Response response) throws IOException {
		// xxl-job admin 请求结果成功
		if(response.isSuccessful()) {
			log.error("xxl-job request successful.");
			ReturnT<T> returnT = JSON.parseObject(response.body().string(), new TypeReference<ReturnT<T>>() {});
			return returnT;
		}
		log.error("xxl-job request fail.");
		// xxl-job admin 请求结果失败
		return new ReturnT<T>(ReturnT.FAIL_CODE, response.toString());
	}

	/*
	 * 字符串拼接
	 *
	 * @param suffix
	 * @return
	 */
	private String joinPath(String suffix) {
		String str = "/";
		String address;
		if (!adminProperties.getAddresses().endsWith(str)) {
			address = adminProperties.getAddresses() + str + suffix;
		}
		address = adminProperties.getAddresses() + suffix;
		return address;
	}
	    
}
