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
import java.util.*;

import com.xxl.job.spring.boot.model.XxlJobGroupList;
import com.xxl.job.spring.boot.model.XxlJobInfo;
import com.xxl.job.spring.boot.model.XxlJobInfoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.spring.boot.annotation.XxlJobCron;
import com.xxl.job.spring.boot.model.XxlJobGroup;

/**
 * TODO
 * @author 		： <a href="https://github.com/hiwepy">wandl</a>
 */
public class XxlJobSpringExecutorWhitRegister extends XxlJobSpringExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(XxlJobSpringExecutorWhitRegister.class);
	private final XxlJobTemplate xxlJobTemplate;
    private String appname;
	private List<XxlJobInfo> cacheJobs = new ArrayList<>();
	private Random RANDOM_ORDER = new Random(10);
	
	public XxlJobSpringExecutorWhitRegister(XxlJobTemplate xxlJobTemplate) {
		this.xxlJobTemplate = xxlJobTemplate;
	}

	@Override
	public void setAppname(String appname) {
		super.setAppname(appname);
		this.appname = appname;
	}

    // start
    @Override
    public void afterSingletonsInstantiated() {

        // init JobHandler Repository
        /*initJobHandlerRepository(applicationContext);*/

        // init JobHandler Repository (for method)
        initJobHandlerMethodRepository(applicationContext);

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        // super start
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        // init job handler from method
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {

            // get bean
            Object bean = null;
            Lazy onBean = applicationContext.findAnnotationOnBean(beanDefinitionName, Lazy.class);
            if (onBean!=null){
                logger.debug("xxl-job annotation scan, skip @Lazy Bean:{}", beanDefinitionName);
                continue;
            }else {
                bean = applicationContext.getBean(beanDefinitionName);
            }

            // filter method
            Map<Method, XxlJob> annotatedMethods = null;   // referred to ：org.springframework.context.event.EventListenerMethodProcessor.processBean
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        new MethodIntrospector.MetadataLookup<XxlJob>() {
                            @Override
                            public XxlJob inspect(Method method) {
                                return AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class);
                            }
                        });
            } catch (Throwable ex) {
                logger.error("xxl-job method-jobhandler resolve error for bean[" + beanDefinitionName + "].", ex);
            }
            if (annotatedMethods==null || annotatedMethods.isEmpty()) {
                continue;
            }

            // generate and regist method job handler
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();
                // regist
                registJobHandler(xxlJob, bean, executeMethod);
                registJobHandlerCronTask(xxlJob, bean, executeMethod);
            }

            registJobHandlerCronTaskTOAdmin();

        }
    }
    
	private void registJobHandlerCronTask(XxlJob xxlJob, Object bean, Method executeMethod) {

        String name = xxlJob.value();
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("xxl-job method-jobhandler name invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
        }

        XxlJobCron xxlJobCron = AnnotationUtils.findAnnotation(executeMethod, XxlJobCron.class);

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

    public void registJobHandlerCronTaskTOAdmin() {

        // 检查执行器是否存在
        if(!StringUtils.hasText(appname)) {
            return;
        }

        // 检查任务组是否存在
        ReturnT<XxlJobGroupList> returnT1 = getXxlJobTemplate().jobInfoGroupList(0, Integer.MAX_VALUE, appname, appname);
        if (returnT1.getCode() == ReturnT.FAIL_CODE) {
            logger.error("执行器查询失败!失败原因:{}", returnT1.getMsg());
            return;
        }
        // 执行器不存在则创建
        XxlJobGroupList jobGroupList = returnT1.getContent();
        Integer jobGroupId = null;
        if(Objects.isNull(jobGroupList) || CollectionUtils.isEmpty(jobGroupList.getData())
                || jobGroupList.getData().stream().noneMatch(xxlJobGroup -> xxlJobGroup.getAppName().equals(appname))) {
            logger.info("执行器'{}'不存在，开始自动添加！", appname);
            // 创建任务组对象
            XxlJobGroup xxlJobGroup = new XxlJobGroup();
            xxlJobGroup.setAppName(appname);
            //xxlJobGroup.setAddressList(addressList);
            xxlJobGroup.setAddressType(0);
            xxlJobGroup.setOrder(RANDOM_ORDER.nextInt(1000));
            //xxlJobGroup.setRegistryList(registryList);
            xxlJobGroup.setTitle(appname);
            ReturnT<String> returnT2 = getXxlJobTemplate().addJobGroup(xxlJobGroup);
            if (returnT2.getCode() == ReturnT.FAIL_CODE) {
                logger.error( "执行器'{}'添加添加失败!失败原因:{}", appname, returnT2.getMsg());
                return;
            } else {
                jobGroupId = Integer.parseInt(returnT2.getContent());
                logger.info("执行器'{}'添加成功", appname);
            }
        } else {
            jobGroupId = jobGroupList.getData().stream().filter(xxlJobGroup -> xxlJobGroup.getAppName().equals(appname)).findFirst().get().getId();
        }
        // 执行器存在或者创建成功，添加定时任务
        for (XxlJobInfo xxlJobInfo : cacheJobs) {

            logger.info(">>>>>>>>>>> xxl-job cron task register jobhandler, name:{}, cron :{}", xxlJobInfo.getExecutorHandler(), xxlJobInfo.getJobCron());
            // 定时任务是否存在
            ReturnT<XxlJobInfoList> returnT3 = getXxlJobTemplate().jobInfoList(0, Integer.MAX_VALUE, jobGroupId);
            if (returnT3.getCode() == ReturnT.FAIL_CODE) {
                logger.error("定时任务查询失败!失败原因:{}", returnT3.getMsg());
                return;
            }
            xxlJobInfo.setJobGroup(jobGroupId);
            XxlJobInfoList jobInfoList = returnT3.getContent();
            if(Objects.isNull(jobInfoList) || CollectionUtils.isEmpty(jobInfoList.getData())
                    || jobInfoList.getData().stream().noneMatch(jobInfo -> {
                        return jobInfo.getScheduleType().equals(xxlJobInfo.getScheduleType())
                                && jobInfo.getJobCron().equals(xxlJobInfo.getJobCron())
                                && jobInfo.getGlueType().compareTo(xxlJobInfo.getGlueType()) == 0
                                && jobInfo.getExecutorHandler().equals(xxlJobInfo.getExecutorHandler())
                                ;
            })) {
                logger.info("不存在 ScheduleType = {}, JobCron = {}, GlueType = {}, ExecutorHandler = {} 的定时任务，开始自动添加！",
                        xxlJobInfo.getScheduleType(), xxlJobInfo.getJobCron(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler());
                // 自动添加定时任务
                ReturnT<Integer> returnT4 =  getXxlJobTemplate().addJob(xxlJobInfo);
                if (returnT4.getCode() == ReturnT.FAIL_CODE) {
                    logger.error("自动添加 ScheduleType = {}, JobCron = {}, GlueType = {}, ExecutorHandler = {} 的定时任务失败!失败原因:{}",
                            xxlJobInfo.getScheduleType(), xxlJobInfo.getJobCron(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler(), returnT3.getMsg());
                } else {
                    logger.info("自动添加 ScheduleType = {}, JobCron = {}, GlueType = {}, ExecutorHandler = {} 的定时任务成功!");
                }
            } else {
                logger.info("存在 ScheduleType = {}, JobCron = {}, GlueType = {}, ExecutorHandler = {} 的定时任务，开始自动更新！",
                        xxlJobInfo.getScheduleType(), xxlJobInfo.getJobCron(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler());
                ReturnT<String> returnT4 =  getXxlJobTemplate().updateJob(xxlJobInfo);
                if (returnT4.getCode() == ReturnT.FAIL_CODE) {
                    logger.error("自动更新 ScheduleType = {}, JobCron = {}, GlueType = {}, ExecutorHandler = {} 的定时任务失败!失败原因:{}",
                            xxlJobInfo.getScheduleType(), xxlJobInfo.getJobCron(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler(), returnT3.getMsg());
                } else {
                    logger.info("自动更新 ScheduleType = {}, JobCron = {}, GlueType = {}, ExecutorHandler = {} 的定时任务成功!");
                }
            }
        }
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
