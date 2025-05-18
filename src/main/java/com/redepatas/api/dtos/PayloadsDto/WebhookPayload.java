package com.redepatas.api.dtos.PayloadsDto;

public record WebhookPayload(
    String event,
    String subscription,
    PaymentPayload payment
) {}
