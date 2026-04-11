package com.lnl.notification.domain.entity;

import com.lnl.notification.domain.enums.NotificationStatus;
import com.lnl.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {

    private final String id;
    private final String orderId;
    private final String customerId;
    private final NotificationType type;
    private final String message;
    private NotificationStatus status;
    private final LocalDateTime createdAt;

    public Notification(String orderId, String customerId, NotificationType type, String message) {
        if (orderId == null || orderId.isBlank())
            throw new IllegalArgumentException("orderId obrigatorio");
        if (customerId == null || customerId.isBlank())
            throw new IllegalArgumentException("customerId obrigatorio");
        if (message == null || message.isBlank())
            throw new IllegalArgumentException("message obrigatoria");

        this.id         = UUID.randomUUID().toString();
        this.orderId    = orderId;
        this.customerId = customerId;
        this.type       = type;
        this.message    = message;
        this.status     = NotificationStatus.PENDING;
        this.createdAt  = LocalDateTime.now();
    }

    // Construtor para reconstituir do banco
    public Notification(String id, String orderId, String customerId,
                        NotificationType type, String message,
                        NotificationStatus status, LocalDateTime createdAt) {
        this.id         = id;
        this.orderId    = orderId;
        this.customerId = customerId;
        this.type       = type;
        this.message    = message;
        this.status     = status;
        this.createdAt  = createdAt;
    }

    public void markAsSent()   { this.status = NotificationStatus.SENT; }
    public void markAsFailed() { this.status = NotificationStatus.FAILED; }

    public String getId()               { return id; }
    public String getOrderId()          { return orderId; }
    public String getCustomerId()       { return customerId; }
    public NotificationType getType()   { return type; }
    public String getMessage()          { return message; }
    public NotificationStatus getStatus(){ return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
