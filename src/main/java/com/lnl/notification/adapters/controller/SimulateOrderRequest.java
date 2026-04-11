package com.lnl.notification.adapters.controller;

import jakarta.validation.constraints.NotBlank;

public record SimulateOrderRequest(
    @NotBlank String orderId,
    @NotBlank String customerId
) {}
