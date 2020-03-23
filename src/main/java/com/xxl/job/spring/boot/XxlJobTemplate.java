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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.TypeReference;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import com.xxl.job.spring.boot.dto.JobBaseModel;
import com.xxl.job.spring.boot.dto.JobPageModel;
import com.xxl.job.spring.boot.dto.XxlJobGroup;
import com.xxl.job.spring.boot.dto.XxlJobInfo;
import com.xxl.job.spring.boot.dto.XxlJobModel;

/**
 * TODO
 * @author 		： <a href="https://github.com/hiwepy">wandl</a>
 */

public class XxlJobTemplate {
	
	private Logger log = LoggerFactory.getLogger(getClass());
    private static String JOBGROUP_SAVE = "/jobgroup/save";
    private static String JOBGROUP_LIST = "/jobgroup/list";
    
	private static String add = "/jobinfo/add";
    private static String update = "/jobinfo/update";
    private static String remove = "/jobinfo/remove";
    private static String start = "/jobinfo/start";
    private static String pause = "/jobinfo/pause";
    private static String resume = "/jobinfo/resume";
    private static String pageList = "/jobinfo/pageList";
    
    private static String getJobInfoByBiz = "/jobinfo/getJobInfoByBiz";

	protected OkHttpTemplate okHttpTemplate;
	protected XxlJobProperties properties;
	protected XxlJobAdminProperties adminProperties;
	protected XxlJobExecutorProperties executorProperties;
	
	public XxlJobTemplate( OkHttpTemplate okHttpTemplate, XxlJobProperties properties) {
		this.properties = properties;
	}
	

/*
    public ResponseEntity<String> addJob(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.getLoadUrl(add), request, String.class, new Object[0]);
        return response;
    }

    public ResponseEntity<String> updateJob(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.getLoadUrl(update), request, String.class, new Object[0]);
        return response;
    }

    public ResponseEntity<String> removeJob(Integer xxlJobInfoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Integer> request = new HttpEntity(xxlJobInfoId, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.getLoadUrl(remove), request, String.class, new Object[0]);
        return response;
    }

    public ResponseEntity<String> pauseJob(int xxlJobInfoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Integer> request = new HttpEntity(xxlJobInfoId, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.getLoadUrl(pause), request, String.class, new Object[0]);
        return response;
    }

    public ResponseEntity<String> resumeJob(int xxlJobInfoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Integer> request = new HttpEntity(xxlJobInfoId, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.getLoadUrl(resume), request, String.class, new Object[0]);
        return response;
    }

    public ResponseEntity<String> getJobInfoByBizJob(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> xxlJobInfoMap = MapUtil.obj2Map(xxlJobInfo);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(xxlJobInfoMap, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(this.getLoadUrl(getJobInfoByBiz), request, String.class, new Object[0]);
        return response;
    }

    public String getLoadUrl(String method) {
        int length = this.jobAdminUrl.length;
        Random random = new Random();
        int i = random.nextInt(length);
        String url = this.jobAdminUrl[i] + method;
        return url;
    }*/
	
	/**
	 * 通用job添加方法
	 * 
	 * @param execuDate
	 * @param desc
	 * @param param
	 * @param author
	 * @param jobHandler
	 * @param businessType
	 */
    public void addJob(Date execuDate,String desc,String param,String author,String jobHandler,int businessType){
        //构建job对象
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        //设置appName
        xxlJobInfo.setAppName(executorProperties.getAppname());
        //设置路由策略
        //xxlJobInfo.setExecutorRouteStrategy(JobEnum.EXECUTOR_ROUTE_STRATEGY_FIRST.getValue());
        //设置job定时器
        //xxlJobInfo.setJobCron(DateUtil.getCronExpression(execuDate));
        //设置运行模式
        //xxlJobInfo.setGlueType(JobEnum.GLUE_TYPE_BEAN.getValue());
        //设置job处理器
        xxlJobInfo.setExecutorHandler(jobHandler);
        //设置job描述
        xxlJobInfo.setJobDesc(desc);
        //设置执行参数
        xxlJobInfo.setExecutorParam(param);
        //设置阻塞处理策略
        //xxlJobInfo.setExecutorBlockStrategy(JobEnum.EXECUTOR_BLOCK_SERIAL_EXECUTION.getValue());
        //设置失败处理策略
        //xxlJobInfo.setExecutorFailStrategy(JobEnum.EXECUTOR_FAIL_STRATEGY_NULL.getValue());
        //设置负责人
        xxlJobInfo.setAuthor(author);
        //设置业务类型
        xxlJobInfo.setBizType(businessType);
        //添加job
        //jobClient.addJob(xxlJobInfo);
    }

	 public void checkAndInitJob(List<XxlJobModel> xxlJobModels, String title, String order) throws HttpProcessException, UnsupportedEncodingException {
	        int jobGroup;
	        //检查并初始化xxl-job执行器
	        Map<String, Object> map = checkAndInitExcutor(title, order);
	        //判断执行器处理返回信息
	        boolean flag = (boolean) map.get("flag");
	        if (flag) {
	            jobGroup = (int) map.get("id");
	            JobPageModel jobPageModel = getJobInfoPageList(jobGroup);
	            if (!ObjectUtils.isEmpty(jobPageModel)) {
	                List<JobBaseModel> list = jobPageModel.getData();
	                //查询数量并启动状态
	                flag = list.stream().map(p -> {
	                    List<JobBaseModel> innerList = new ArrayList<>();
	                    xxlJobModels.stream().map(x -> {
	                        if (x.getExecutorHandlerName().equals(p.getExecutorHandler())) {
	                            if (p.getTriggerStatus() == 0) {
	                                startJob(p);
	                            }
	                            innerList.add(p);
	                        }
	                        return null;
	                    }).count();
	                    return innerList;
	                }).count() > 0;
	                if (flag) {
	                    return;
	                }
	                //执行任务添加
	                for (XxlJobModel xxlJobModel : xxlJobModels) {
	                    ReturnT<String> returnT = addJobInfo(xxlJobModel, jobGroup);
	                    if (returnT.getCode() == ReturnT.FAIL_CODE) {
	                        log.error(title + "定时任务添加添加失败!失败原因:{}", returnT.getMsg());
	                    } else {
	                        log.error(title + "定时任务添加添加成功!");
	                        jobPageModel = getJobInfoPageList(jobGroup);
	                        if (!ObjectUtils.isEmpty(jobPageModel)) {
	                            list = jobPageModel.getData();
	                            list.stream().map(p -> {
	                                if (xxlJobModel.getExecutorHandlerName().equals(p.getExecutorHandler())) {
	                                    if (p.getTriggerStatus() == 0) {
	                                        startJob(p);
	                                    }
	                                }
	                                return null;
	                            });
	                        }
	                    }
	                }
	            } else {
	                log.error("xxl-job任务获取列表接口异常!");
	            }
	        }
	    }

	    /**
	     * 检查并初始化xxl-job执行器
	     *
	     * @param title 执行器标题
	     * @return
	     * @throws HttpProcessException
	     */
	    private Map<String, Object> checkAndInitExcutor(String title, String order) {
	        Map<String, Object> map = new HashMap<>();
	        map.put("flag", true);
	        List<XxlJobGroup> excutorList = jobgroupList();
	        //如果没有该执行器则插入执行器
	        if (CollectionUtils.isEmpty(excutorList)) {
	            String exeSaveResult = HttpUtils.get(joinPath(StringUtils.join("jobgroup/save?appName=", appName,
	                    "&title=", title, "&addressType=0&order=" + order)), null);
	            	ReturnT<String> returnT = JSON.parseObject(exeSaveResult, new TypeReference<ReturnT<String>>() {
	            });
	            if (returnT.getCode() == ReturnT.FAIL_CODE) {
	                map.remove("flag");
	                map.put("flag", false);
	                log.error(title + "注册失败!失败原因:{}", returnT.getMsg());
	                return map;
	            } else {
	                log.error(title + "注册成功!");
	                excutorList = jobgroupList();
	            }
	        }
	        map.put("id", excutorList.get(0).getId());
	        return map;
	    }

	/**
	 * 获取执行器列表并进行过滤
	 *
	 * @return
	 * @throws HttpProcessException
	 */
	private List<XxlJobGroup> jobgroupList() {
		
		// 获取xxl-job 执行器列表数据
		final List<XxlJobGroup> excutorList = null;

		Map<String, Object> params = new HashMap<>();

		params.put("start", "0");
		params.put("length", "100");
		params.put("jobDesc", "");
		params.put("executorHandler", "");
		params.put("author", "");
		params.put("triggerStatus", "-1");

		okHttpTemplate.get(JOBGROUP_LIST, params, (response) -> {
			
			try {
				excutorList = JSONObject.parseArray(response.body().string(), XxlJobGroup.class);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// response.body().string();
			// JSON.parseObject(jobListStr, JobPageModel.class);

			return true;
		});

		return excutorList.stream().filter(p -> "".equals(p.getAppName())).collect(Collectors.toList());
	}

	/**
	 * 获取指定执行器下任务分页列表数据
	 *
	 * @param jobGroup
	 * @return
	 * @throws HttpProcessException
	 */
	private JobPageModel getJobInfoPageList(int jobGroup) {

		Map<String, Object> params = new HashMap<>();

		params.put("start", "0");
		params.put("length", "100");
		params.put("jobDesc", "");
		params.put("executorHandler", "");
		params.put("author", "");
		params.put("triggerStatus", "-1");
		params.put("jobGroup", jobGroup);

		okHttpTemplate.get(pageList, params, (response) -> {

			JobPageModel model = JSONObject.parseObject(response.body().string(), JobPageModel.class);
			
			// response.body().string();
			// JSON.parseObject(jobListStr, JobPageModel.class);

			return true;
		});

		return null;
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
	private ReturnT<String> addJobInfo(XxlJobModel xxlJobModel, int jobGroup) {

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

		okHttpTemplate.get(add, params, (response) -> {

			// response.body().string();
			
			return true;
		});

		return returnT;
	}

	/**
	 * 启动任务
	 *
	 * @param jobBaseModel 任务实体
	 */
	private void startJob(JobBaseModel jobBaseModel) {
		Map<String, Object> params = new HashMap<>();
		params.put("id", jobBaseModel.getId());
		okHttpTemplate.get(start, params, (response) -> {
			System.out.println(response.body().string());
			return true;
		});
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
