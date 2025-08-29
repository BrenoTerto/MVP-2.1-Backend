package com.redepatas.api.cliente.dtos.PayloadsDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(
        String event,
        String subscription,
        PaymentPayload payment) {
}