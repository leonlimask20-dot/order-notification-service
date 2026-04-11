package com.lnl.notification.adapters.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consome mensagens que falharam 3x no topico principal.
 * Evita que mensagens ruins travem o consumer indefinidamente.
 */
@Component
public class DLQConsumer {

    private static final Logger log = LoggerFactory.getLogger(DLQConsumer.class);

    @KafkaListener(
        topics = "${kafka.topics.order-placed}.DLT",
        groupId = "${spring.kafka.consumer.group-id}-dlq"
    )
    public void onOrderPlacedDLQ(String rawMessage) {
        log.error("Mensagem na DLQ (order-placed): {}", rawMessage);
        // Aqui poderia: salvar no banco com status FAILED, alertar equipe, etc.
    }

    @KafkaListener(
        topics = "${kafka.topics.order-cancelled}.DLT",
        groupId = "${spring.kafka.consumer.group-id}-dlq"
    )
    public void onOrderCancelledDLQ(String rawMessage) {
        log.error("Mensagem na DLQ (order-cancelled): {}", rawMessage);
    }
}
