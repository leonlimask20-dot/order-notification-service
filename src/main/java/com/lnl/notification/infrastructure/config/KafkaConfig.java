package com.lnl.notification.infrastructure.config;

import com.lnl.notification.adapters.kafka.event.OrderCancelledEvent;
import com.lnl.notification.adapters.kafka.event.OrderPlacedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.order-placed}")
    private String orderPlacedTopic;

    @Value("${kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    // Criacao automatica dos topicos ao subir a aplicacao
    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(orderPlacedTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(orderCancelledTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderPlacedDLT() {
        return TopicBuilder.name(orderPlacedTopic + ".DLT").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledDLT() {
        return TopicBuilder.name(orderCancelledTopic + ".DLT").partitions(1).replicas(1).build();
    }

    // Error handler: tenta 3x com 2s de intervalo, depois manda para a DLQ
    private DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> orderPlacedListenerFactory(
            ConsumerFactory<String, OrderPlacedEvent> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> orderCancelledListenerFactory(
            ConsumerFactory<String, OrderCancelledEvent> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }
}
