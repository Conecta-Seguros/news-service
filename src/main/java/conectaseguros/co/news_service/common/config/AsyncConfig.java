package conectaseguros.co.news_service.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${processing.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${processing.async.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${processing.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${processing.async.thread-name-prefix:file-processor-}")
    private String threadNamePrefix;

    @Bean(name = "fileProcessorExecutor")
    public TaskExecutor fileProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

}
