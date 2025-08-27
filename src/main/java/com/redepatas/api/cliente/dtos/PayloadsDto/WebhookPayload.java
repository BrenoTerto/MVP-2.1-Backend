package com.redepatas.api.cliente.dtos.PayloadsDto;

public record WebhookPayload(
        String event,
        String subscription,
        PaymentPayload payment) {
}
