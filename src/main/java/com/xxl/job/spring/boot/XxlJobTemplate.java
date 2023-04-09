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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxl.job.spring.boot.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.xxl.job.core.biz.model.ReturnT;

public class XxlJobTemplate {
	


	protected RestTemplate restTemplate;
	protected XxlJobProperties properties;
	protected XxlJobAdminProperties adminProperties;
	protected XxlJobExecutorProperties executorProperties;
	
	public XxlJobTemplate( RestTemplate restTemplate, XxlJobProperties properties, 
			XxlJobAdminProperties adminProperties, 
			XxlJobExecutorProperties executorProperties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
		this.adminProperties = adminProperties;
		this.executorProperties = executorProperties;
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBGROUP_PAGELIST), request, String.class);
		// xxl-job admin 请求结果成功
		if(response.getStatusCode().is2xxSuccessful()) {
			return new ReturnT<XxlJobGroupList>(ReturnT.FAIL_CODE, JSON.parseObject(response.getBody(), new TypeReference<XxlJobGroupList>() {
			}));
		}
		// xxl-job admin 请求结果失败
		return new ReturnT<XxlJobGroupList>(ReturnT.FAIL_CODE, response.toString());
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBGROUP_GET), request, String.class);
		// xxl-job admin 请求结果成功
		if(response.getStatusCode().is2xxSuccessful()) {
			return JSON.parseObject(response.getBody(), new TypeReference<ReturnT<XxlJobGroup>>() {
			});
		}
		// xxl-job admin 请求结果失败
		return new ReturnT<XxlJobGroup>(ReturnT.FAIL_CODE, response.toString());
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBGROUP_SAVE), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBGROUP_UPDATE), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBGROUP_REMOVE), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start	起始位置
	 * @param length 数量
	 * @param jobGroup 执行器主键ID
	 * @return
	 */
	public ReturnT<XxlJobInfoList> jobInfoList(int start, int length, Integer jobGroup) {
		return this.jobInfoList(start, length, jobGroup, -1, "", "", "");
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start	起始位置
	 * @param length 数量
	 * @param jobGroup 执行器主键ID
	 * @param triggerStatus 调度状态：0-停止，1-运行
	 * @return
	 */
    public ReturnT<XxlJobInfoList> jobInfoList(int start, int length, Integer jobGroup, int triggerStatus) {
    	return this.jobInfoList(start, length, jobGroup, triggerStatus, "", "", "");
	}

	/**
	 * 获取xxl-job 执行器列表数据
	 * @param start
	 * @param length
	 * @param jobGroup
	 * @param triggerStatus
	 * @param jobDesc
	 * @param executorHandler
	 * @param author
	 * @return
	 */
    public ReturnT<XxlJobInfoList> jobInfoList(int start, int length, Integer jobGroup,
			int triggerStatus, String jobDesc, String executorHandler, String author) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = new HashMap<>(7);
		paramMap.put("start", Math.max(0, start));
		paramMap.put("length", Math.min(length, 5));
		paramMap.put("jobGroup", jobGroup);
		paramMap.put("triggerStatus", triggerStatus);
		paramMap.put("jobDesc", jobDesc);
		paramMap.put("executorHandler", executorHandler);
		paramMap.put("author", author);
		// xxl-job admin 请求体
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_PAGELIST), request, String.class);
		// xxl-job admin 请求结果成功
		if(response.getStatusCode().is2xxSuccessful()) {
			return new ReturnT<XxlJobInfoList>(ReturnT.FAIL_CODE, JSON.parseObject(response.getBody(), new TypeReference<XxlJobInfoList>() {
			}));
		}
		// xxl-job admin 请求结果失败
		return new ReturnT<XxlJobInfoList>(ReturnT.FAIL_CODE, response.toString());
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_ADD), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity2(response);
    }
    
    /*public ResponseEntity<String> addJobOrUpdate(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XxlJobConstants.XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_ADD_UPDATE), request, String.class);
        return response;
    }*/

	/**
	 * 修改调度任务
	 * @param jobInfo 调用任务信息Model
	 * @return
	 */
    public ReturnT<String> updateJob(XxlJobInfo jobInfo) {
		// xxl-job admin 请求参数
		Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(jobInfo), Map.class);
		// xxl-job admin 请求体
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_UPDATE), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_REMOVE), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_STOP), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_START), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
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
		HttpEntity<Map<String, Object>> request = this.buildRequestEntity(paramMap);
		// xxl-job admin 请求操作
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(XxlJobConstants.JOBINFO_TRIGGER), request, String.class);
		// xxl-job admin 请求结果处理
		return this.parseResponseEntity(response);
    }


	private HttpEntity<Map<String, Object>> buildRequestEntity(Map<String, Object> paramMap) {
		// xxl-job admin 请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(XxlJobConstants.XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
		HttpEntity<Map<String, Object>> requestBody = new HttpEntity<>(paramMap, headers);
		return requestBody;
	}

	private ReturnT<String> parseResponseEntity(ResponseEntity<String> response){
		// xxl-job admin 请求结果成功
		if(response.getStatusCode().is2xxSuccessful()) {
			return JSON.parseObject(response.getBody(), new TypeReference<ReturnT<String>>() {
			});
		}
		// xxl-job admin 请求结果失败
		return new ReturnT<String>(ReturnT.FAIL_CODE, response.toString());
	}

	private ReturnT<Integer> parseResponseEntity2(ResponseEntity<String> response){
		// xxl-job admin 请求结果成功
		if(response.getStatusCode().is2xxSuccessful()) {
			return JSON.parseObject(response.getBody(), new TypeReference<ReturnT<Integer>>() {
			});
		}
		// xxl-job admin 请求结果失败
		return new ReturnT<Integer>(ReturnT.FAIL_CODE, response.toString());
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
