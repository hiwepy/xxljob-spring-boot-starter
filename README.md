# xxljob-spring-boot-starter
xxljob-spring-boot-starter

#### 组件简介

- 基于 [xxljob ](https://github.com/xuxueli/xxl-job/) 的API封装（有改过xxl-job）
- 实现客户端自动注册任务
- 该客户端需要基于修改后的xxl-job [jeebiz-xxljob ](https://github.com/Jeebiz/jeebiz-xxljob) 作为服务端，才可以使用

#### 使用说明

##### 1、Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>xxljob-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

##### 2、在`application.properties`文件中增加如下配置

```properties
##########################XXL-JOB执行器参数定义##################################
### 是否启用 XXL-Job 执行器
xxl.job.enabled=true
### 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
xxl.job.admin.addresses=http://localhost:8091/xxl-job-admin
### 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
xxl.job.executor.appname=${spring.application.name}
xxl.job.executor.title=${spring.application.name}
### 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 "执行器注册" 和 "调度中心请求并触发任务"；
xxl.job.executor.ip=
### 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；
xxl.job.executor.port=-1
### 执行器通讯TOKEN [选填]：非空时启用；
xxl.job.accessToken=0Izqw61KQ6
### 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
xxl.job.executor.logpath=/data/applogs/xxl-job/jobhandler
### 执行器日志保存天数 [选填] ：值大于3时生效，启用执行器Log文件定期清理功能，否则不生效；
xxl.job.executor.logretentiondays=30
```

##### 3、使用示例

腾讯云短信（Short Message Service，SMS）

```java

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.ShardingUtil;
import com.xxl.job.spring.boot.annotation.XxlJobCron;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 * 1、在Spring Bean实例中，开发Job方法，方式格式要求为 "public ReturnT<String> execute(String param)"
 * 2、为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobLogger.log" 打印执行日志；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
public class SampleXxlJob {
    private static Logger logger = LoggerFactory.getLogger(SampleXxlJob.class);


    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("testJobHandler")
    @XxlJobCron(cron = "* * 0 * * ?", desc = "简单任务示例（Bean模式）", author = "hiwepy")
    public ReturnT<String> demoJobHandler(String param) throws Exception {
        XxlJobLogger.log("XXL-JOB, Hello World.");

        for (int i = 0; i < 5; i++) {
            XxlJobLogger.log("beat at:" + i);
            TimeUnit.SECONDS.sleep(2);
        }
        return ReturnT.SUCCESS;
    }


    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    @XxlJobCron(cron = "* * 0 * * ?", desc = "分片广播任务", author = "hiwepy")
    public ReturnT<String> shardingJobHandler(String param) throws Exception {

        // 分片参数
        ShardingUtil.ShardingVO shardingDTO = ShardingUtil.getShardingVo();
        XxlJobLogger.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardingDTO.getIndex(), shardingDTO.getTotal());

        // 业务逻辑
        for (int i = 0; i < shardingDTO.getTotal(); i++) {
            if (i == shardingDTO.getIndex()) {
                XxlJobLogger.log("第 {} 片, 命中分片开始处理", i);
            } else {
                XxlJobLogger.log("第 {} 片, 忽略", i);
            }
        }

        return ReturnT.SUCCESS;
    }


    /**
     * 3、命令行任务
     */
    @XxlJob("commandJobHandler")
    @XxlJobCron(cron = "* * 0 * * ?", desc = "命令行任务", author = "hiwepy")
    public ReturnT<String> commandJobHandler(String param) throws Exception {
        String command = param;
        int exitValue = -1;

        BufferedReader bufferedReader = null;
        try {
            // command process
            Process process = Runtime.getRuntime().exec(command);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

            // command log
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                XxlJobLogger.log(line);
            }

            // command exit
            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            XxlJobLogger.log(e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        if (exitValue == 0) {
            return IJobHandler.SUCCESS;
        } else {
            return new ReturnT<String>(IJobHandler.FAIL.getCode(), "command exit value("+exitValue+") is failed");
        }
    }


    /**
     * 4、跨平台Http任务
     */
    @XxlJob("httpJobHandler")
    @XxlJobCron(cron = "* * 0 * * ?", desc = "跨平台Http任务", author = "hiwepy")
    public ReturnT<String> httpJobHandler(String param) throws Exception {

        // request
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(param);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(5 * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // do connection
            connection.connect();

            //Map<String, List<String>> map = connection.getHeaderFields();

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String responseMsg = result.toString();

            XxlJobLogger.log(responseMsg);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log(e);
            return ReturnT.FAIL;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                XxlJobLogger.log(e2);
            }
        }

    }

    /**
     * 5、生命周期任务示例：任务初始化与销毁时，支持自定义相关逻辑；
     */
    @XxlJob(value = "demoJobHandler2", init = "init", destroy = "destroy")
    @XxlJobCron(cron = "* * 0 * * ?", desc = "生命周期任务示例：任务初始化与销毁时，支持自定义相关逻辑", author = "hiwepy")
    public ReturnT<String> demoJobHandler2(String param) throws Exception {
        XxlJobLogger.log("XXL-JOB, Hello World.");
        return ReturnT.SUCCESS;
    }
    
    public void init(){
        logger.info("init");
    }
    
    public void destroy(){
        logger.info("destory");
    }

}
```
 

## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|