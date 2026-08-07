package com.xxl.job.spring.boot.metrics;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * XXL-Job 执行器级 Micrometer 指标绑定器。
 *
 * <p>兼容存在 {@code TriggerCallbackThread.callBackQueue} 的旧版 core；新版 core 移除该内部结构时
 * 安全跳过队列指标，不影响 starter 启动。</p>
 */
@Slf4j
public class XxlJobMetrics implements MeterBinder, ApplicationListener<ApplicationStartedEvent> {

    public static final String XXL_JOB_METRIC_NAME_PREFIX = "xxl";
    public static final String METRIC_NAME_JOB_REQUESTS_SUBMITTED = XXL_JOB_METRIC_NAME_PREFIX + ".job.submitted";
    public static final String METRIC_NAME_JOB_REQUESTS_RUNNING = XXL_JOB_METRIC_NAME_PREFIX + ".job.running";
    public static final String METRIC_NAME_JOB_REQUESTS_COMPLETED = XXL_JOB_METRIC_NAME_PREFIX + ".job.completed";
    public static final String METRIC_NAME_JOB_REQUESTS_DURATION = XXL_JOB_METRIC_NAME_PREFIX + ".job.duration";
    public static final String METRIC_NAME_JOB_QUEUE_SIZE = XXL_JOB_METRIC_NAME_PREFIX + ".job.queue.size";

    private final List<Tag> tags;

    /**
     * 创建不带额外标签的指标绑定器。
     *
     * @param executor XXL-Job Spring 执行器
     */
    public XxlJobMetrics(XxlJobSpringExecutor executor) {
        this(executor, Collections.emptyList());
    }

    /**
     * 创建带额外标签的指标绑定器。
     *
     * @param executor XXL-Job Spring 执行器
     * @param tags 指标标签，可为空
     */
    public XxlJobMetrics(XxlJobSpringExecutor executor, Iterable<Tag> tags) {
        Objects.requireNonNull(executor, "executor must not be null");
        List<Tag> copiedTags = new ArrayList<>();
        if (Objects.nonNull(tags)) {
            for (Tag tag : tags) {
                copiedTags.add(tag);
            }
        }
        this.tags = Collections.unmodifiableList(copiedTags);
    }

    /**
     * 应用启动后绑定指标。
     *
     * @param event Spring Boot 应用启动事件
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        MeterRegistry registry = event.getApplicationContext().getBean(MeterRegistry.class);
        bindTo(registry);
    }

    /**
     * 将可用的 XXL-Job 执行器指标绑定到注册表。
     *
     * @param registry 指标注册表
     */
    @Override
    public void bindTo(MeterRegistry registry) {
        bindCallbackQueueSizeIfSupported(Objects.requireNonNull(registry, "registry must not be null"));
    }

    private void bindCallbackQueueSizeIfSupported(MeterRegistry registry) {
        try {
            Class<?> callbackThreadClass = Class.forName("com.xxl.job.core.thread.TriggerCallbackThread");
            Method getInstance = callbackThreadClass.getMethod("getInstance");
            Object callbackThread = getInstance.invoke(null);
            if (Objects.isNull(callbackThread)) {
                return;
            }
            Gauge.builder(
                            METRIC_NAME_JOB_QUEUE_SIZE,
                            callbackThread,
                            value -> callbackQueueSize(callbackThreadClass, value))
                    .description("The size of the XXL-Job callback queue")
                    .tags(tags)
                    .register(registry);
        } catch (ClassNotFoundException exception) {
            log.debug("TriggerCallbackThread not found; skip XXL-Job callback queue metric");
        } catch (ReflectiveOperationException exception) {
            log.warn("XXL-Job callback queue metric unavailable", exception);
        }
    }

    private double callbackQueueSize(Class<?> callbackThreadClass, Object callbackThread) {
        try {
            Field field = ReflectionUtils.findField(callbackThreadClass, "callBackQueue");
            if (Objects.isNull(field)) {
                return 0;
            }
            ReflectionUtils.makeAccessible(field);
            Object queue = field.get(callbackThread);
            if (queue instanceof LinkedBlockingQueue) {
                return ((LinkedBlockingQueue<?>) queue).size();
            }
        } catch (IllegalAccessException exception) {
            log.warn("Unable to read XXL-Job callback queue", exception);
        }
        return 0;
    }
}
