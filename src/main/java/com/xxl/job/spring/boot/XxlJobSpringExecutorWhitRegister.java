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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.handler.impl.MethodJobHandler;
import com.xxl.job.spring.boot.annotation.XxlJobCron;
import com.xxl.job.spring.boot.dto.XxlJobGroup;
import com.xxl.job.spring.boot.dto.XxlJobInfo;

/**
 * TODO
 * @author 		： <a href="https://github.com/hiwepy">wandl</a>
 */
public class XxlJobSpringExecutorWhitRegister extends XxlJobSpringExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(XxlJobSpringExecutorWhitRegister.class);
	private final XxlJobTemplate xxlJobTemplate;
	/**
	 * 	执行器AppName [必填]：执行器心跳注册分组依据；为空则关闭自动注册.
	 */
	private String appName;
	private List<XxlJobInfo> cacheJobs = new ArrayList<>(); 
	private Random RANDOM_ORDER = new Random(10);
	
	public XxlJobSpringExecutorWhitRegister(XxlJobTemplate xxlJobTemplate) {
		this.xxlJobTemplate = xxlJobTemplate;
	}

	@Override
	public void setAppName(String appName) {
		super.setAppName(appName);
		this.appName = appName;
	}
    
	public String getAppName() {
		return appName;
	}

	// start
    @Override
    public void afterPropertiesSet() throws Exception {

        // init JobHandler Repository
        initJobHandlerRepository(applicationContext);

        // init JobHandler Repository (for method)
        initJobHandlerMethodRepository(applicationContext);

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        // this start
        this.start();
    }

    @Override
    public void start() throws Exception {
    	super.start();
    	
    	if(!StringUtils.hasText(this.getAppName())) {
    		return;
    	}
    	
    	XxlJobGroup xxlJobGroup = new XxlJobGroup();
    	xxlJobGroup.setAppName(this.getAppName());
    	//xxlJobGroup.setAddressList(addressList);
    	xxlJobGroup.setAddressType(0);
    	xxlJobGroup.setOrder(RANDOM_ORDER.nextInt(1000));
    	//xxlJobGroup.setRegistryList(registryList);
    	xxlJobGroup.setTitle(this.getAppName());
    	
		ResponseEntity<String> response1 =  getXxlJobTemplate().addOrUpdateGroup(xxlJobGroup);
		if(response1.getStatusCode().is2xxSuccessful()) {
			ReturnT<String> returnT1 = JSON.parseObject(response1.getBody(), new TypeReference<ReturnT<String>>() {});
			if (returnT1.getCode() == ReturnT.FAIL_CODE) {
		    	logger.error(this.getAppName() + "执行器添加添加失败!失败原因:{}", returnT1.getMsg());
		    } else {
		    	logger.error(this.getAppName() + "执行器添加添加成功!");
		    	for (XxlJobInfo xxlJobInfo : cacheJobs) {

		            logger.info(">>>>>>>>>>> xxl-job cron task register jobhandler, name:{}, cron :{}", xxlJobInfo.getExecutorHandler(), xxlJobInfo.getJobCron());

		            xxlJobInfo.setJobGroup(Integer.parseInt(returnT1.getContent()));
		    		ResponseEntity<String> response =  getXxlJobTemplate().addJob(xxlJobInfo);
		    		if(response.getStatusCode().is2xxSuccessful()) {
		    			 String jobStr = response.getBody();
		    			 ReturnT<String> returnT = JSON.parseObject(jobStr, new TypeReference<ReturnT<String>>() {
		    		     });
		    			 
		    		     if (returnT.getCode() == ReturnT.FAIL_CODE) {
		    		    	 logger.error(xxlJobInfo.getExecutorHandler() + "定时任务添加添加失败!失败原因:{}", returnT.getMsg());
		    		     } else {
		    		    	 logger.error(xxlJobInfo.getExecutorHandler() + "定时任务添加添加成功!");
		    		    	 getXxlJobTemplate().startJob(Integer.parseInt(returnT.getContent()));
		    		     }
		    		}
				}
		    }
			
			
		}
		
		
    	
    }
    
    // destroy
    @Override
    public void destroy() {
        super.destroy();
    }

    @SuppressWarnings("deprecation")
	private void initJobHandlerRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }

        // init job handler action
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(JobHandler.class);

        if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                if (serviceBean instanceof IJobHandler) {
                	try {
	                	JobHandler xxlJob = serviceBean.getClass().getAnnotation(JobHandler.class);
	                    String name = xxlJob.value();
	                    IJobHandler handler = (IJobHandler) serviceBean;
	                    if (loadJobHandler(name) != null) {
	                        throw new RuntimeException("xxl-job jobhandler[" + name + "] naming conflicts.");
	                    }
	                    XxlJobCron xxlJobCron = serviceBean.getClass().getAnnotation(XxlJobCron.class);
	                    if (xxlJob != null && xxlJobCron != null ) {
	                    	 registJobHandler(name, handler);
	                         registJobHandlerCronTask(name, xxlJobCron);
	                    }
                	} catch (Exception e) {
        				e.printStackTrace();
        			}
                }
            }
        }
    }

    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        	
        // init job handler from method
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {

        	try {
	        	
	            Object bean = applicationContext.getBean(beanDefinitionName);
	            Method[] methods = bean.getClass().getDeclaredMethods();
	            for (Method method: methods) {
	                XxlJob xxlJob = AnnotationUtils.findAnnotation(method, XxlJob.class);
	                XxlJobCron xxlJobCron = AnnotationUtils.findAnnotation(method, XxlJobCron.class);
	                if (xxlJob != null && xxlJobCron != null ) {
	                	
	                    // name
	                    String name = xxlJob.value();
	                    if (name.trim().length() == 0) {
	                        throw new RuntimeException("xxl-job method-jobhandler name invalid, for[" + bean.getClass() + "#"+ method.getName() +"] .");
	                    }
	                    if (loadJobHandler(name) != null) {
	                        throw new RuntimeException("xxl-job jobhandler[" + name + "] naming conflicts.");
	                    }
	                    
	                    // execute method
	                    if (!(method.getParameterTypes()!=null && method.getParameterTypes().length==1 && method.getParameterTypes()[0].isAssignableFrom(String.class))) {
	                        throw new RuntimeException("xxl-job method-jobhandler param-classtype invalid, for[" + bean.getClass() + "#"+ method.getName() +"] , " +
	                                "The correct method format like \" public ReturnT<String> execute(String param) \" .");
	                    }
	                    if (!method.getReturnType().isAssignableFrom(ReturnT.class)) {
	                        throw new RuntimeException("xxl-job method-jobhandler return-classtype invalid, for[" + bean.getClass() + "#"+ method.getName() +"] , " +
	                                "The correct method format like \" public ReturnT<String> execute(String param) \" .");
	                    }
	                    method.setAccessible(true);
	
	                    // init and destory
	                    Method initMethod = null;
	                    Method destroyMethod = null;
	
	                    if(xxlJob.init().trim().length() > 0) {
	                        try {
	                            initMethod = bean.getClass().getDeclaredMethod(xxlJob.init());
	                            initMethod.setAccessible(true);
	                        } catch (NoSuchMethodException e) {
	                            throw new RuntimeException("xxl-job method-jobhandler initMethod invalid, for[" + bean.getClass() + "#"+ method.getName() +"] .");
	                        }
	                    }
	                    if(xxlJob.destroy().trim().length() > 0) {
	                        try {
	                            destroyMethod = bean.getClass().getDeclaredMethod(xxlJob.destroy());
	                            destroyMethod.setAccessible(true);
	                        } catch (NoSuchMethodException e) {
	                            throw new RuntimeException("xxl-job method-jobhandler destroyMethod invalid, for[" + bean.getClass() + "#"+ method.getName() +"] .");
	                        }
	                    }
	                    // registry jobhandler
	                    registJobHandler(name, new MethodJobHandler(bean, method, initMethod, destroyMethod));
	                    registJobHandlerCronTask(name, xxlJobCron);
	                    
	                }
	            }

	        } catch (Exception e) {
				e.printStackTrace();
			}
        }
        
    }
    
	private void registJobHandlerCronTask(String name, XxlJobCron xxlJobCron) {
    	
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        
        xxlJobInfo.setAuthor(xxlJobCron.author());
        xxlJobInfo.setAlarmEmail(xxlJobCron.alarmEmail());
        xxlJobInfo.setExecutorBlockStrategy(xxlJobCron.blockStrategy().name());
        xxlJobInfo.setExecutorFailRetryCount(xxlJobCron.failRetryCount());
        xxlJobInfo.setExecutorHandler(name);
        xxlJobInfo.setExecutorParam(xxlJobCron.param());
        xxlJobInfo.setExecutorRouteStrategy(xxlJobCron.routeStrategy().name());
        xxlJobInfo.setExecutorTimeout(xxlJobCron.timeout());
        xxlJobInfo.setGlueType(GlueTypeEnum.BEAN.name());
        xxlJobInfo.setJobCron(xxlJobCron.cron());
        xxlJobInfo.setJobDesc(xxlJobCron.desc());
        
        cacheJobs.add(xxlJobInfo);
        
    }
    
	public XxlJobTemplate getXxlJobTemplate() {
		return xxlJobTemplate;
	}
	
    // ---------------------- applicationContext ----------------------
	public ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
}
