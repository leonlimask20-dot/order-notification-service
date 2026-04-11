package com.lnl.notification.adapters.kafka.event;

// DTO que representa o evento publicado pelo order-processing-api
public record OrderPlacedEvent(
    String orderId,
    String customerId,
    double total
) {}
