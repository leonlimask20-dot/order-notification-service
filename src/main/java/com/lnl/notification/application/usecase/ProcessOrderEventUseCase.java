package com.lnl.notification.application.usecase;

import com.lnl.notification.domain.entity.Notification;
import com.lnl.notification.domain.enums.NotificationType;
import com.lnl.notification.domain.repository.NotificationRepository;

public class ProcessOrderEventUseCase {

    private final NotificationRepository notificationRepository;

    public ProcessOrderEventUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification execute(String orderId, String customerId, NotificationType type) {
        String message = buildMessage(orderId, type);
        Notification notification = new Notification(orderId, customerId, type, message);
        notification.markAsSent();
        return notificationRepository.save(notification);
    }

    // TODO: implemente aqui a logica de gerar a mensagem da notificacao
    // Dica: use orderId e type para criar uma mensagem descritiva para o cliente
    // Exemplo de resultado esperado para ORDER_PLACED:
    //   "Seu pedido ABC-123 foi recebido e esta sendo processado."
    private String buildMessage(String orderId, NotificationType type) {
        return switch (type) {
            case ORDER_PLACED    -> "Seu pedido " + orderId + " foi recebido e esta sendo processado.";
            case ORDER_CANCELLED -> "Seu pedido " + orderId + " foi cancelado.";
        };
    }
}
