package com.lnl.notification.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface SpringDataNotificationRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findByOrderId(String orderId);
}
