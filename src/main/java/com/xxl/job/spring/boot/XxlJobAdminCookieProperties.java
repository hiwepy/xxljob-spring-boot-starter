package com.xxl.job.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 调度中心登录状态Cookie缓存配置
 */

@ConfigurationProperties(XxlJobAdminCookieProperties.PREFIX)
@Data
public class XxlJobAdminCookieProperties {

    public static final String PREFIX = "xxl.job.admin.cookie";

    /**
     * he maximum size of the cache
     */
    private long maximumSize = 10_000;

    /**
     * the length of time after an entry is created that it should be automatically removed
     */
    private Duration expireAfterWrite = Duration.ofMinutes(30);

    /**
     * the length of time after an entry is created that it should be automatically removed
     */
    private Duration expireAfterAccess = Duration.ofMinutes(30);

}
