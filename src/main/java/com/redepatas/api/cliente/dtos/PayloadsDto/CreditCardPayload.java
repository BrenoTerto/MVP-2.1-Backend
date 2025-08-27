package com.redepatas.api.cliente.dtos.PayloadsDto;

public record CreditCardPayload(
    String creditCardNumber,
    String creditCardBrand,
    String creditCardToken
) {}