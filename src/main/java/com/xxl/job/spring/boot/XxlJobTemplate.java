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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.spring.boot.dto.MapUtil;
import com.xxl.job.spring.boot.dto.XxlJobGroup;
import com.xxl.job.spring.boot.dto.XxlJobInfo;
import com.xxl.job.spring.boot.dto.XxlJobModel;

public class XxlJobTemplate {
	
	private static String XXL_RPC_ACCESS_TOKEN = "XXL-RPC-ACCESS-TOKEN";
    private static String JOBGROUP_SAVE = "/jobgroup/save";
    private static String JOBGROUP_SAVE_UPDATE = "/jobgroup/saveOrUpdate";
    private static String JOBGROUP_UPDATE = "/jobgroup/update";
    private static String JOBGROUP_REMOVE = "/jobgroup/remove";
    private static String JOBGROUP_GET = "/jobgroup/loadById";
    
    private static String JOBINFO_PAGELIST = "/jobinfo/pageList";
	private static String JOBINFO_ADD = "/jobinfo/add";
	private static String JOBINFO_ADD_UPDATE = "/jobinfo/addOrUpdate";
    private static String JOBINFO_UPDATE = "/jobinfo/update";
    private static String JOBINFO_REMOVE = "/jobinfo/remove";
    private static String JOBINFO_STOP = "/jobinfo/stop";
    private static String JOBINFO_START = "/jobinfo/start";
    private static String JOBINFO_TRIGGER = "/jobinfo/trigger";

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
	 */
    public ResponseEntity<String> jobinfoList(int start, int length, int jobGroup, 
			int triggerStatus) {
    	return this.jobinfoList(start, length, jobGroup, triggerStatus, "", "", "");
	}
	
	/**
	 * 获取xxl-job 执行器列表数据
	 */
    public ResponseEntity<String> jobinfoList(int start, int length, int jobGroup, 
			int triggerStatus, String jobDesc, String executorHandler, String author) {

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

		params.put("start", Collections.singletonList(Math.max(0, start)));
		params.put("length", Collections.singletonList(Math.min(length, 5)));
		params.put("jobGroup", Collections.singletonList(jobGroup));
		params.put("triggerStatus", Collections.singletonList(triggerStatus));
		params.put("jobDesc", Collections.singletonList(jobDesc));
		params.put("executorHandler", Collections.singletonList(executorHandler));
		params.put("author", Collections.singletonList(author));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_PAGELIST),
				request, String.class);
		
		return response;
		  
	}
    
    public ResponseEntity<String> addJob(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_ADD), request, String.class);
        return response;
    }
    
    public ResponseEntity<String> addJobOrUpdate(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_ADD_UPDATE), request, String.class);
        return response;
    }

    public ResponseEntity<String> updateJob(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_UPDATE), request, String.class);
        return response;
    }

    public ResponseEntity<String> removeJob(int jobInfoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.put("id", Collections.singletonList(String.valueOf(jobInfoId)));
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_REMOVE), request, String.class);
        return response;
    }
    
    public ResponseEntity<String> stopJob(int jobInfoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.put("id", Collections.singletonList(String.valueOf(jobInfoId)));
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_STOP), request, String.class);
        return response;
    }
    
    public ResponseEntity<String> startJob(int jobInfoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.put("id", Collections.singletonList(String.valueOf(jobInfoId)));
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_START), request, String.class);
        return response;
    }
    
    public ResponseEntity<String> triggerJob(int jobInfoId, String executorParam) {
    	
    	MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();

	    paramMap.put("id", Collections.singletonList(String.valueOf(jobInfoId)));
		paramMap.put("executorParam", Collections.singletonList(executorParam));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBINFO_TRIGGER), request, String.class);
        return response;
    }

	public ResponseEntity<String> addGroup(XxlJobGroup xxlJobGroup) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
		MultiValueMap<String, String> xxlJobGroupMap = MapUtil.obj2Map(xxlJobGroup);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobGroupMap, headers);
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBGROUP_SAVE), request, String.class);
		
		return response;
	}

	public ResponseEntity<String> addOrUpdateGroup(XxlJobGroup xxlJobGroup) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
		MultiValueMap<String, String> xxlJobGroupMap = MapUtil.obj2Map(xxlJobGroup);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobGroupMap, headers);
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBGROUP_SAVE_UPDATE), request, String.class);
		
		return response;
	}
	
	public ResponseEntity<String> updateGroup(XxlJobGroup xxlJobGroup) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
		MultiValueMap<String, String> xxlJobGroupMap = MapUtil.obj2Map(xxlJobGroup);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(xxlJobGroupMap, headers);
		ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBGROUP_UPDATE), request, String.class);
		
		return response;
	}
	
    public ResponseEntity<String> removeGroup(int jobGroupId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        HttpEntity<Integer> request = new HttpEntity<Integer>(jobGroupId, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBGROUP_REMOVE), request, String.class);
        return response;
    }
    
    public ResponseEntity<String> getGroup(int jobGroupId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(XXL_RPC_ACCESS_TOKEN, properties.getAccessToken());
        HttpEntity<Integer> request = new HttpEntity<Integer>(jobGroupId, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.joinPath(JOBGROUP_GET), request, String.class);
        return response;
    }

	/**
	 * 添加任务信息
	 *
	 * @param xxlJobModel 任务信息实体
	 * @param jobGroup    执行器编号
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws HttpProcessException
	 */
    public ReturnT<String> addJobInfo(XxlJobModel xxlJobModel, int jobGroup) throws UnsupportedEncodingException {

		Map<String, Object> params = new HashMap<>();

		params.put("jobGroup", jobGroup);
		params.put("jobDesc", URLEncoder.encode(xxlJobModel.getJobDesc(), "UTF-8"));
		params.put("executorRouteStrategy", "FIRST");
		params.put("executorBlockStrategy", "SERIAL_EXECUTION");
		params.put("jobCron", URLEncoder.encode(xxlJobModel.getJobCron(), "utf-8"));

		params.put("glueType", "BEAN");
		params.put("executorHandler", xxlJobModel.getExecutorHandlerName());
		params.put("childJobId", "");
		params.put("executorTimeout", "30");
		params.put("executorFailRetryCount", "3");
		params.put("author", xxlJobModel.getAuthor());
		params.put("alarmEmail", "");
		params.put("executorParam", "");
		params.put("glueRemark", URLEncoder.encode("GLUE代码初始化", "UTF-8"));
		params.put("glueSource", "");

		ReturnT<String> returnT = null;
 

		return returnT;
	}

	 
	/**
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
