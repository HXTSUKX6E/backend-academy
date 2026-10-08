package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SchedulerConfiguration {

    private ExecutorService executorService;

    @Bean
    public ExecutorService schedulerExecutorService(SchedulerProperties schedulerProperties) {
        executorService = Executors.newFixedThreadPool(schedulerProperties.getThreadCount());
        return executorService;
    }

    @PreDestroy
    public void shutdown() {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
