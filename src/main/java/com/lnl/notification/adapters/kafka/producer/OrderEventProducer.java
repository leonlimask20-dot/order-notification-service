package com.lnl.notification.adapters.kafka.producer;

import com.lnl.notification.adapters.kafka.event.OrderCancelledEvent;
import com.lnl.notification.adapters.kafka.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Simula o order-processing-api publicando eventos.
 * Em producao, este producer estaria no servico de pedidos, nao aqui.
 */
@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-placed}")
    private String orderPlacedTopic;

    @Value("${kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(String orderId, String customerId, double total) {
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, total);
        kafkaTemplate.send(orderPlacedTopic, orderId, event);
        log.info("Evento publicado: ORDER_PLACED | orderId={}", orderId);
    }

    public void publishOrderCancelled(String orderId, String customerId, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, customerId, reason);
        kafkaTemplate.send(orderCancelledTopic, orderId, event);
        log.info("Evento publicado: ORDER_CANCELLED | orderId={}", orderId);
    }
}
