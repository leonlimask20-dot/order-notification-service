package com.lnl.notification.domain.repository;

import com.lnl.notification.domain.entity.Notification;
import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findAll();
    List<Notification> findByOrderId(String orderId);
}
