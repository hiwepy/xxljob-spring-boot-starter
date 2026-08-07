package com.xxl.job.spring.boot;

import com.xxl.job.core.XxlJobTemplate;
import com.xxl.job.core.annotation.XxlJobCron;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.XxlJobHandlerRegistrar;
import com.xxl.job.spring.XxlJobAutoBindingSpringExecutor;
import com.xxl.job.spring.boot.metrics.MetricMethodJobHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 带 Micrometer 指标采集能力的 XXL-Job 自动绑定执行器。
 *
 * <p>任务扫描和 Admin 自动注册复用 {@link XxlJobAutoBindingSpringExecutor}，仅将方法处理器
 * 替换为 {@link MetricMethodJobHandler}，确保 Spring Boot 专属指标能力留在 starter 中。</p>
 */
public class XxlJobAutoBindingAndMetricsSpringExecutor extends XxlJobAutoBindingSpringExecutor {

    private final MeterRegistry registry;
    private final List<Tag> tags;

    /**
     * 创建带指标采集能力的自动绑定执行器。
     *
     * @param registry 指标注册表
     * @param xxlJobTemplate XXL-Job Admin 操作模板
     * @param tags 所有任务共享的附加标签，可为空
     */
    public XxlJobAutoBindingAndMetricsSpringExecutor(
            MeterRegistry registry,
            XxlJobTemplate xxlJobTemplate,
            Collection<Tag> tags) {
        super(Objects.requireNonNull(xxlJobTemplate, "xxlJobTemplate must not be null"));
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.tags = Objects.isNull(tags)
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(tags));
    }

    /**
     * 注册带指标包装的 XXL-Job 方法处理器。
     *
     * @param xxlJob 标准 XXL-Job 注解，可为空
     * @param bean 任务方法所属 Bean
     * @param executeMethod 任务执行方法
     */
    @Override
    protected void registerJobHandlerInternal(XxlJob xxlJob, Object bean, Method executeMethod) {
        Objects.requireNonNull(bean, "bean must not be null");
        Objects.requireNonNull(executeMethod, "executeMethod must not be null");

        XxlJobCron xxlJobCron = AnnotationUtils.findAnnotation(executeMethod, XxlJobCron.class);
        String name = resolveHandlerName(xxlJob, xxlJobCron);
        if (!StringUtils.hasText(name)) {
            return;
        }

        if (Objects.nonNull(loadJobHandler(name))) {
            throw new IllegalStateException("xxl-job jobhandler[" + name + "] naming conflicts.");
        }

        executeMethod.setAccessible(true);
        String initMethodName = resolveInitMethodName(xxlJob, xxlJobCron);
        String destroyMethodName = resolveDestroyMethodName(xxlJob, xxlJobCron);
        Method initMethod = resolveLifecycleMethod(bean.getClass(), executeMethod, initMethodName, "initMethod");
        Method destroyMethod = resolveLifecycleMethod(bean.getClass(), executeMethod, destroyMethodName, "destroyMethod");

        // 通过 extension 提供的兼容层注册，屏蔽 xxl-job-core 不同版本的静态/实例 API 差异。
        MetricMethodJobHandler handler = new MetricMethodJobHandler(
                registry, bean, executeMethod, initMethod, destroyMethod, tags);
        XxlJobHandlerRegistrar.registerJobHandler(this, name, handler);
    }

    private String resolveHandlerName(XxlJob xxlJob, XxlJobCron xxlJobCron) {
        if (Objects.nonNull(xxlJobCron) && StringUtils.hasText(xxlJobCron.value())) {
            return xxlJobCron.value();
        }
        if (Objects.nonNull(xxlJob) && StringUtils.hasText(xxlJob.value())) {
            return xxlJob.value();
        }
        return null;
    }

    private String resolveInitMethodName(XxlJob xxlJob, XxlJobCron xxlJobCron) {
        if (Objects.nonNull(xxlJobCron) && StringUtils.hasText(xxlJobCron.value())) {
            return xxlJobCron.init();
        }
        return Objects.nonNull(xxlJob) ? xxlJob.init() : null;
    }

    private String resolveDestroyMethodName(XxlJob xxlJob, XxlJobCron xxlJobCron) {
        if (Objects.nonNull(xxlJobCron) && StringUtils.hasText(xxlJobCron.value())) {
            return xxlJobCron.destroy();
        }
        return Objects.nonNull(xxlJob) ? xxlJob.destroy() : null;
    }

    private Method resolveLifecycleMethod(
            Class<?> beanClass,
            Method executeMethod,
            String lifecycleMethodName,
            String lifecycleRole) {
        if (!StringUtils.hasText(lifecycleMethodName)) {
            return null;
        }
        try {
            Method lifecycleMethod = beanClass.getDeclaredMethod(lifecycleMethodName);
            lifecycleMethod.setAccessible(true);
            return lifecycleMethod;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(
                    "xxl-job method-jobhandler " + lifecycleRole + " invalid, for["
                            + beanClass + "#" + executeMethod.getName() + "].",
                    exception);
        }
    }
}
