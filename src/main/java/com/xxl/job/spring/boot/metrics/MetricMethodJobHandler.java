package com.xxl.job.spring.boot.metrics;

import com.xxl.job.core.annotation.XxlJobCron;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 为方法型 XXL-Job Handler 采集提交数、运行数、完成数和执行耗时。
 *
 * <p>所有指标使用稳定名称，并通过 {@code job} 标签区分处理器，避免动态指标名造成高基数。</p>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@Slf4j
public class MetricMethodJobHandler extends IJobHandler {

    private final Object target;
    private final Method method;
    private final Method initMethod;
    private final Method destroyMethod;
    private final String jobName;
    private final Counter submitted;
    private final AtomicInteger running;
    private final Counter completed;
    private final Timer duration;

    /**
     * 创建带 Micrometer 指标的任务方法处理器。
     *
     * @param registry 指标注册表
     * @param target 任务方法所属对象
     * @param method 任务执行方法
     * @param initMethod 初始化方法，可为空
     * @param destroyMethod 销毁方法，可为空
     * @param tags 共享指标标签，可为空
     */
    public MetricMethodJobHandler(
            MeterRegistry registry,
            Object target,
            Method method,
            Method initMethod,
            Method destroyMethod,
            Collection<Tag> tags) {
        MeterRegistry requiredRegistry = Objects.requireNonNull(registry, "registry must not be null");
        this.target = Objects.requireNonNull(target, "target must not be null");
        this.method = Objects.requireNonNull(method, "method must not be null");
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
        this.jobName = resolveJobName(method);

        List<Tag> jobTags = Objects.isNull(tags) ? new ArrayList<>() : new ArrayList<>(tags);
        jobTags.add(Tag.of("job", jobName));
        List<Tag> immutableTags = Collections.unmodifiableList(jobTags);
        this.submitted = requiredRegistry.counter(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_SUBMITTED, immutableTags);
        this.running = Objects.requireNonNull(
                requiredRegistry.gauge(
                        XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_RUNNING,
                        immutableTags,
                        new AtomicInteger()),
                "running gauge must not be null");
        this.completed = requiredRegistry.counter(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_COMPLETED, immutableTags);
        this.duration = requiredRegistry.timer(XxlJobMetrics.METRIC_NAME_JOB_REQUESTS_DURATION, immutableTags);
    }

    /**
     * 执行目标任务并在成功或异常路径上完整记录指标。
     *
     * @throws Exception 反射调用任务方法失败时抛出
     */
    @Override
    public void execute() throws Exception {
        long startNanos = System.nanoTime();
        submitted.increment();
        running.incrementAndGet();
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 0) {
                method.invoke(target, new Object[parameterTypes.length]);
            } else {
                method.invoke(target);
            }
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            duration.record(elapsedNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
            running.decrementAndGet();
            completed.increment();
            log.info("xxl-job handler completed, job={}, durationNanos={}", jobName, elapsedNanos);
        }
    }

    /**
     * 调用可选的初始化方法。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Override
    public void init() throws Exception {
        if (Objects.nonNull(initMethod)) {
            initMethod.invoke(target);
        }
    }

    /**
     * 调用可选的销毁方法。
     *
     * @throws Exception 反射调用失败时抛出
     */
    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(destroyMethod)) {
            destroyMethod.invoke(target);
        }
    }

    @Override
    /**
     * to String.
     *
     * @return the result
     */
    public String toString() {
        return super.toString() + "[" + target.getClass() + "#" + method.getName() + "]";
    }

    private String resolveJobName(Method targetMethod) {
        XxlJobCron xxlJobCron = AnnotatedElementUtils.findMergedAnnotation(targetMethod, XxlJobCron.class);
        if (Objects.nonNull(xxlJobCron) && StringUtils.hasText(xxlJobCron.value())) {
            return xxlJobCron.value();
        }
        XxlJob xxlJob = AnnotatedElementUtils.findMergedAnnotation(targetMethod, XxlJob.class);
        if (Objects.nonNull(xxlJob) && StringUtils.hasText(xxlJob.value())) {
            return xxlJob.value();
        }
        return targetMethod.getName();
    }
}
