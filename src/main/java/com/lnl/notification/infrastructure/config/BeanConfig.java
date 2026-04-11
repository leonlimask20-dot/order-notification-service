package com.lnl.notification.infrastructure.config;

import com.lnl.notification.application.usecase.ProcessOrderEventUseCase;
import com.lnl.notification.domain.repository.NotificationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ProcessOrderEventUseCase processOrderEventUseCase(NotificationRepository repo) {
        return new ProcessOrderEventUseCase(repo);
    }
}
