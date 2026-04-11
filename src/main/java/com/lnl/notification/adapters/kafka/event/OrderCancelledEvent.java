package com.lnl.notification.adapters.kafka.event;

public record OrderCancelledEvent(
    String orderId,
    String customerId,
    String reason
) {}
