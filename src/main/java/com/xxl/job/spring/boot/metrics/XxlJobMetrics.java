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
package com.xxl.job.spring.boot.metrics;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * XXL Job Metrics
 */
public class XxlJobMetrics implements MeterBinder, ApplicationListener<ApplicationStartedEvent> {

	/**
	 * Prefix used for all XXL Job metric names.
	 */
	public static final String XXL_JOB_METRIC_NAME_PREFIX = "xxl-job";

	/**
	 * dispatcher
	 */
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS 			= XXL_JOB_METRIC_NAME_PREFIX + ".job.max.requests";
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST 	= XXL_JOB_METRIC_NAME_PREFIX + ".dispatcher.max.requests.perhost";
	public static final String METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT		= XXL_JOB_METRIC_NAME_PREFIX + ".dispatcher.queued.calls.count";
	public static final String METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT		= XXL_JOB_METRIC_NAME_PREFIX + ".job.running.calls.count";

	/**
	 * http cache
	 */
	public static final String METRIC_NAME_CACHE_REQUEST_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".job.request.count";
	public static final String METRIC_NAME_CACHE_HIT_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".cache.hit.count";
	public static final String METRIC_NAME_CACHE_NETWORK_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".cache.network.count";
	public static final String METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".job.write.success.count";
	public static final String METRIC_NAME_CACHE_WRITE_ABORT_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".job.abort.count";
	public static final String METRIC_NAME_CACHE_CURRENT_SIZE 	= XXL_JOB_METRIC_NAME_PREFIX + ".cache.current.size";
	public static final String METRIC_NAME_CACHE_MAX_SIZE 	= XXL_JOB_METRIC_NAME_PREFIX + ".cache.max.size";

	/**
	 * connection pool
	 */
	public static final String METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT		= XXL_JOB_METRIC_NAME_PREFIX + ".connection.pool.connection.count";
	public static final String METRIC_NAME_CONNECTION_POOL_IDLE_CONNECTION_COUNT		= XXL_JOB_METRIC_NAME_PREFIX + ".connection.pool.idle.connection.count";

	/**
	 * timeout
	 */
	public static final String METRIC_NAME_CALL_TIMEOUT_COUNT 			= XXL_JOB_METRIC_NAME_PREFIX + ".call.timeout.count";
	public static final String METRIC_NAME_CONNECT_TIMEOUT_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".connect.timeout.count";
	public static final String METRIC_NAME_READ_TIMEOUT_COUNT 			= XXL_JOB_METRIC_NAME_PREFIX + ".read.timeout.count";
	public static final String METRIC_NAME_WRITE_TIMEOUT_COUNT 		= XXL_JOB_METRIC_NAME_PREFIX + ".write.timeout.count";
	public static final String METRIC_NAME_PING_FAIL_COUNT 			= XXL_JOB_METRIC_NAME_PREFIX + ".ping.fail.count";

	/**
	 * job
	 */
	public static final String METRIC_NAME_JOB_REQUESTS_SUBMITTED 			= XXL_JOB_METRIC_NAME_PREFIX + ".job.submitted";
	public static final String METRIC_NAME_JOB_REQUESTS_RUNNING 			= XXL_JOB_METRIC_NAME_PREFIX + ".job.running";
	public static final String METRIC_NAME_JOB_REQUESTS_COMPLETED 			= XXL_JOB_METRIC_NAME_PREFIX + ".job.completed";
	public static final String METRIC_NAME_JOB_REQUESTS_DURATION 			= XXL_JOB_METRIC_NAME_PREFIX + ".job.duration";

	private XxlJobSpringExecutor executor;
	private Iterable<Tag> tags;

	public XxlJobMetrics(XxlJobSpringExecutor executor) {
		this(executor, Collections.emptyList());
	}

	public XxlJobMetrics(XxlJobSpringExecutor executor, Iterable<Tag> tags) {
		this.executor = executor;
		this.tags = tags;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.bindTo(event.getApplicationContext().getBean(MeterRegistry.class));
	}

	@Override
	public void bindTo(MeterRegistry registry) {

		// bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS, "max requests of dispatcher ", dispatcher, Dispatcher::getMaxRequests);
	}



	private <T> void bindTimer(MeterRegistry registry, String name, String desc, T metricsHandler,
						   ToLongFunction<T> countFunc, ToDoubleFunction<T> consumer) {
		FunctionTimer.builder(name, metricsHandler, countFunc, consumer, TimeUnit.SECONDS)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	private <T> void bindGauge(MeterRegistry registry, String name, String desc, T metricResult,
								   ToDoubleFunction<T> consumer) {
		Gauge.builder(name, metricResult, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}
	
	private <T> void bindTimeGauge(MeterRegistry registry, String name, String desc, T metricResult,
							   ToDoubleFunction<T> consumer) {
		TimeGauge.builder(name, metricResult, TimeUnit.SECONDS, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	private <T> void bindCounter(MeterRegistry registry, String name, String desc, T metricsHandler,
							 ToDoubleFunction<T> consumer) {
		FunctionCounter.builder(name, metricsHandler, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

}
