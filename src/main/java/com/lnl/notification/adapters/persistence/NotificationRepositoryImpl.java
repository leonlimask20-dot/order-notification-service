package com.lnl.notification.adapters.persistence;

import com.lnl.notification.domain.entity.Notification;
import com.lnl.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final SpringDataNotificationRepository jpa;

    public NotificationRepositoryImpl(SpringDataNotificationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Notification save(Notification n) {
        NotificationEntity entity = new NotificationEntity(
            n.getId(), n.getOrderId(), n.getCustomerId(),
            n.getType(), n.getMessage(), n.getStatus(), n.getCreatedAt()
        );
        return toDomain(jpa.save(entity));
    }

    @Override
    public List<Notification> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Notification> findByOrderId(String orderId) {
        return jpa.findByOrderId(orderId).stream().map(this::toDomain).toList();
    }

    private Notification toDomain(NotificationEntity e) {
        return new Notification(e.getId(), e.getOrderId(), e.getCustomerId(),
                                e.getType(), e.getMessage(), e.getStatus(), e.getCreatedAt());
    }
}
