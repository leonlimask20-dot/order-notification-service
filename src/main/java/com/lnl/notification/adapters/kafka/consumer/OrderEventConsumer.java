package com.lnl.notification.adapters.kafka.consumer;

import com.lnl.notification.adapters.kafka.event.OrderCancelledEvent;
import com.lnl.notification.adapters.kafka.event.OrderPlacedEvent;
import com.lnl.notification.application.usecase.ProcessOrderEventUseCase;
import com.lnl.notification.domain.enums.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProcessOrderEventUseCase processOrderEvent;

    public OrderEventConsumer(ProcessOrderEventUseCase processOrderEvent) {
        this.processOrderEvent = processOrderEvent;
    }

    @KafkaListener(
        topics = "${kafka.topics.order-placed}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "orderPlacedListenerFactory"
    )
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Evento recebido: ORDER_PLACED | orderId={} customerId={}", event.orderId(), event.customerId());
        processOrderEvent.execute(event.orderId(), event.customerId(), NotificationType.ORDER_PLACED);
    }

    @KafkaListener(
        topics = "${kafka.topics.order-cancelled}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "orderCancelledListenerFactory"
    )
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Evento recebido: ORDER_CANCELLED | orderId={} customerId={}", event.orderId(), event.customerId());
        processOrderEvent.execute(event.orderId(), event.customerId(), NotificationType.ORDER_CANCELLED);
    }
}
