package com.lnl.notification.adapters.persistence;

import com.lnl.notification.domain.enums.NotificationStatus;
import com.lnl.notification.domain.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected NotificationEntity() {}

    public NotificationEntity(String id, String orderId, String customerId,
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

    public String getId()                { return id; }
    public String getOrderId()           { return orderId; }
    public String getCustomerId()        { return customerId; }
    public NotificationType getType()    { return type; }
    public String getMessage()           { return message; }
    public NotificationStatus getStatus(){ return status; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
}
