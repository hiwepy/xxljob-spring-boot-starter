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

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.spring.boot.annotation.XxlJobCron;
import com.xxl.job.spring.boot.executor.ScheduleTypeEnum;
import com.xxl.job.spring.boot.model.XxlJobGroup;
import com.xxl.job.spring.boot.model.XxlJobGroupList;
import com.xxl.job.spring.boot.model.XxlJobInfo;
import com.xxl.job.spring.boot.model.XxlJobInfoList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * TODO
 * @author 		： <a href="https://github.com/hiwepy">wandl</a>
 */
@Slf4j
public class XxlJobSpringExecutorWhitRegister extends XxlJobSpringExecutor {
	
	private final XxlJobTemplate xxlJobTemplate;
    private String appName;
    private String appTitle;
	private List<XxlJobInfo> cacheJobs = new ArrayList<>();
	private Random RANDOM_ORDER = new Random(10);
	
	public XxlJobSpringExecutorWhitRegister(XxlJobTemplate xxlJobTemplate) {
		this.xxlJobTemplate = xxlJobTemplate;
	}

	@Override
	public void setAppname(String appName) {
		super.setAppname(appName);
		this.appName = appName;
	}

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
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
                log.debug("xxl-job annotation scan, skip @Lazy Bean:{}", beanDefinitionName);
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
                log.error("xxl-job method-jobhandler resolve error for bean[" + beanDefinitionName + "].", ex);
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

            registJobHandlerCronTaskToAdmin();

        }
    }
    
	private void registJobHandlerCronTask(XxlJob xxlJob, Object bean, Method executeMethod) {

        String name = xxlJob.value();
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("xxl-job method-jobhandler name invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
        }

        XxlJobCron xxlJobCron = AnnotationUtils.findAnnotation(executeMethod, XxlJobCron.class);

        if(Objects.isNull(xxlJobCron)) {
        	return;
        }

        XxlJobInfo xxlJobInfo = new XxlJobInfo();

        // 任务描述
        xxlJobInfo.setJobDesc(xxlJobCron.desc());
        // 负责人
        xxlJobInfo.setAuthor(xxlJobCron.author());
        // 报警邮件
        xxlJobInfo.setAlarmEmail(xxlJobCron.alarmEmail());
        // 调度类型
        xxlJobInfo.setScheduleType(xxlJobCron.scheduleType().name());
        // Cron
        xxlJobInfo.setScheduleConf(xxlJobCron.cron());
        // 运行模式
        xxlJobInfo.setGlueType(xxlJobCron.glueType().name());
        // JobHandler
        xxlJobInfo.setExecutorHandler(name);
        // 任务参数
        xxlJobInfo.setExecutorParam(xxlJobCron.param());
        // 路由策略
        xxlJobInfo.setExecutorRouteStrategy(xxlJobCron.routeStrategy().name());
        // 失败重试次数
        xxlJobInfo.setExecutorFailRetryCount(xxlJobCron.failRetryCount());
        // 调度过期策略
        xxlJobInfo.setMisfireStrategy(xxlJobCron.misfireStrategy().name());
        // 阻塞处理策略
        xxlJobInfo.setExecutorBlockStrategy(xxlJobCron.blockStrategy().name());
        // 任务超时时间
        xxlJobInfo.setExecutorTimeout(xxlJobCron.timeout());
        cacheJobs.add(xxlJobInfo);
        
    }

    public void registJobHandlerCronTaskToAdmin() {

        // 检查执行器是否存在
        if(!StringUtils.hasText(appName)) {
            return;
        }

        // 检查任务组是否存在
        ReturnT<XxlJobGroupList> returnT1 = getXxlJobTemplate().jobInfoGroupList(0, Integer.MAX_VALUE, appName, null);
        if (returnT1.getCode() == ReturnT.FAIL_CODE) {
            log.error(">>>>>>>>>>> 执行器查询失败!失败原因:{}", returnT1.getMsg());
            return;
        }
        // 执行器不存在则创建
        XxlJobGroupList jobGroupList = returnT1.getContent();
        Integer jobGroupId = null;
        if(Objects.isNull(jobGroupList) || CollectionUtils.isEmpty(jobGroupList.getData())
                || jobGroupList.getData().stream().noneMatch(xxlJobGroup -> xxlJobGroup.getAppName().equals(appName))) {
            log.info(">>>>>>>>>>> 执行器'{}'不存在，开始自动添加！", appName);
            // 创建任务组对象
            XxlJobGroup xxlJobGroup = new XxlJobGroup();
            xxlJobGroup.setAppName(appName);
            xxlJobGroup.setAddressType(0);
            xxlJobGroup.setOrder(RANDOM_ORDER.nextInt(1000));
            xxlJobGroup.setTitle(appTitle);
            ReturnT<String> returnT2 = getXxlJobTemplate().addJobGroup(xxlJobGroup);
            if (returnT2.getCode() == ReturnT.FAIL_CODE) {
                log.error( ">>>>>>>>>>> 执行器'{}'添加添加失败!失败原因:{}", appName, returnT2.getMsg());
                return;
            }
            returnT1 = getXxlJobTemplate().jobInfoGroupList(0, Integer.MAX_VALUE, appName, null);
            if (returnT1.getCode() == ReturnT.FAIL_CODE) {
                log.error(">>>>>>>>>>> 执行器查询失败!失败原因:{}", returnT1.getMsg());
                return;
            }
            jobGroupList = returnT1.getContent();
        } else {
            jobGroupId = jobGroupList.getData().stream().filter(xxlJobGroup -> xxlJobGroup.getAppName().equals(appName)).findFirst().get().getId();
        }
        // 定时任务是否存在
        ReturnT<XxlJobInfoList> returnT3 = getXxlJobTemplate().jobInfoList(0, Integer.MAX_VALUE, jobGroupId);
        if (returnT3.getCode() == ReturnT.FAIL_CODE) {
            log.error(">>>>>>>>>>> 定时任务查询失败!失败原因:{}", returnT3.getMsg());
            return;
        }
        XxlJobInfoList jobInfoList = returnT3.getContent();

        // 执行器存在或者创建成功，添加定时任务
        for (XxlJobInfo xxlJobInfo : cacheJobs) {

            log.info(">>>>>>>>>>> xxl-job cron task register jobhandler, name:{}, cron :{}", xxlJobInfo.getExecutorHandler(), xxlJobInfo.getScheduleConf());

            if(Objects.isNull(jobInfoList) || CollectionUtils.isEmpty(jobInfoList.getData())
                    || jobInfoList.getData().stream().noneMatch(jobInfo -> jobInfo.getExecutorHandler().equals(xxlJobInfo.getExecutorHandler())
            )) {
                log.info(">>>>>>>>>>> 不存在 ScheduleType = {}, ScheduleConf = {}, GlueType = {}, ExecutorHandler = {} 的定时任务，开始自动添加！",
                        xxlJobInfo.getScheduleType(), xxlJobInfo.getScheduleConf(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler());
                // 自动添加定时任务
                ReturnT<Integer> returnT4 =  getXxlJobTemplate().addJob(xxlJobInfo);
                if (returnT4.getCode() == ReturnT.FAIL_CODE) {
                    log.error(">>>>>>>>>>> 自动添加 ScheduleType = {}, ScheduleConf = {}, GlueType = {}, ExecutorHandler = {} 的定时任务失败!失败原因:{}",
                            xxlJobInfo.getScheduleType(), xxlJobInfo.getScheduleConf(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler(), returnT3.getMsg());
                } else {
                    log.info(">>>>>>>>>>> 自动添加 ScheduleType = {}, ScheduleConf = {}, GlueType = {}, ExecutorHandler = {} 的定时任务成功!");
                }
            } else {
                Optional<XxlJobInfo> optional = jobInfoList.getData().stream().filter(jobInfo -> jobInfo.getExecutorHandler().equals(xxlJobInfo.getExecutorHandler())).findFirst();
                xxlJobInfo.setId(optional.get().getId());

                log.info(">>>>>>>>>>> 存在 JobId = {}, ScheduleType = {}, ScheduleConf = {}, GlueType = {}, ExecutorHandler = {} 的定时任务，开始自动更新！",
                        xxlJobInfo.getId(), xxlJobInfo.getScheduleType(), xxlJobInfo.getScheduleConf(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler());

                ReturnT<String> returnT4 =  getXxlJobTemplate().updateJob(xxlJobInfo);
                if (returnT4.getCode() == ReturnT.FAIL_CODE) {
                    log.error(">>>>>>>>>>> 自动更新 ScheduleType = {}, ScheduleConf = {}, GlueType = {}, ExecutorHandler = {} 的定时任务失败!失败原因:{}",
                            xxlJobInfo.getScheduleType(), xxlJobInfo.getScheduleConf(), xxlJobInfo.getGlueType(), xxlJobInfo.getExecutorHandler(), returnT3.getMsg());
                } else {
                    log.info(">>>>>>>>>>> 自动更新 ScheduleType = {}, ScheduleConf = {}, GlueType = {}, ExecutorHandler = {} 的定时任务成功!");
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
