package com.finder.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 스케줄러 스레드 풀 설정.
 * 기본 스케줄러는 단일 스레드라 한 외부 API 갱신이 재시도로 지연되면 다른 갱신까지 밀린다.
 * 병상·기본정보·차단메시지·중증질환 갱신이 서로 독립적으로 돌도록 풀로 분리한다.
 */
@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("finder-sched-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(20);
        return scheduler;
    }
}
