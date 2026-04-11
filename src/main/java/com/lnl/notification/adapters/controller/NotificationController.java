package com.lnl.notification.adapters.controller;

import com.lnl.notification.adapters.kafka.producer.OrderEventProducer;
import com.lnl.notification.domain.entity.Notification;
import com.lnl.notification.domain.repository.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final OrderEventProducer producer;

    public NotificationController(NotificationRepository notificationRepository,
                                  OrderEventProducer producer) {
        this.notificationRepository = notificationRepository;
        this.producer               = producer;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> listAll() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }

    @GetMapping("/notifications/order/{orderId}")
    public ResponseEntity<List<Notification>> listByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(notificationRepository.findByOrderId(orderId));
    }

    // Simula o order-processing-api publicando um evento de pedido criado
    @PostMapping("/simulate/order-placed")
    public ResponseEntity<String> simulateOrderPlaced(@Valid @RequestBody SimulateOrderRequest req) {
        String orderId = req.orderId().isBlank() ? UUID.randomUUID().toString() : req.orderId();
        producer.publishOrderPlaced(orderId, req.customerId(), 99.90);
        return ResponseEntity.accepted().body("Evento ORDER_PLACED publicado: " + orderId);
    }

    // Simula o order-processing-api publicando um evento de pedido cancelado
    @PostMapping("/simulate/order-cancelled")
    public ResponseEntity<String> simulateOrderCancelled(@Valid @RequestBody SimulateOrderRequest req) {
        producer.publishOrderCancelled(req.orderId(), req.customerId(), "Cancelado pelo cliente");
        return ResponseEntity.accepted().body("Evento ORDER_CANCELLED publicado: " + req.orderId());
    }
}
