package com.xxl.job.spring.boot.aspect;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.spring.boot.annotation.XxlJobCron;
import com.xxl.job.spring.boot.metrics.MetricNames;
import com.xxl.job.spring.boot.metrics.XxlJobMetrics;
import io.micrometer.common.util.StringUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Aspect
@Slf4j
public class XxlJobExecutorAspect {

	private MeterRegistry registry;
	private Collection<Tag> tags;
	private final Counter submitted;
	private final Counter running;
	private final Counter completed;
	private final Timer duration;

	public XxlJobExecutorAspect(MeterRegistry registry, Collection<Tag> tags) {
		this.registry = registry;
		this.tags = Objects.isNull(tags) ? Collections.emptyList() : tags;
		this.submitted = registry.counter(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_SUBMITTED, this.tags);
		this.running = registry.counter(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING, this.tags);
		this.completed = registry.counter(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED, this.tags);
		this.duration = registry.timer(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_DURATION, this.tags);
	}

	@Around("@annotation(com.xxl.job.core.handler.annotation.XxlJob) && @annotation(job)")
	public Object aroundMethod(ProceedingJoinPoint pjd, XxlJob job) throws Throwable {
		// 记录请求开始时间
		long start = System.currentTimeMillis();
		// 一次请求计数 +1
		submitted.increment();
		// 当前正在运行的请求数 +1
		running.increment();

		// 1、获取AOP信息
		Signature signature = pjd.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;

		// 2、获取方法及参数信息
		Method method = methodSignature.getMethod();

		// 3、获取 XxlJobCron 注解
		String metric = MetricNames.name(XxlJobMetrics.XXL_JOB_METRIC_NAME_PREFIX, job.value());
		List<Tag> jobTags = new ArrayList<>(tags);
		jobTags.add(Tag.of("job", job.value()));
		Timer timer = registry.timer(metric, jobTags);

		// 1、创建并启动 StopWatch
		StopWatch stopWatch = new StopWatch(job.value());
		XxlJobCron jobCron = AnnotationUtils.findAnnotation(method, XxlJobCron.class);
		stopWatch.start(Objects.nonNull(jobCron) ? jobCron.desc() : job.value());

		try {
			// 3、执行代理方法
			Object result = pjd.proceed();
			if(stopWatch.isRunning()){
				stopWatch.stop();
			}
			return result;
		} catch (Throwable ex) {
			if(stopWatch.isRunning()){
				stopWatch.stop();
			}
			throw ex;
		} finally {
			// 记录本次请求耗时
			timer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
			duration.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
			// 当前正在运行的请求数 -1
			running.increment(-1);
			// 当前已完成的请求数 +1
			completed.increment();
			// 2、记录方法执行时间
			log.info(stopWatch.prettyPrint());
		}
	}

}


