package com.redepatas.api.cliente.dtos.PayloadsDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentPayload(
                String id,
                String status,
                String externalReference,
                String description,
                String billingType,
                String confirmedDate,
                String customer,
                String subscription,
                String creditDate,
                String estimatedCreditDate,
                CreditCardPayload creditCard) {
}