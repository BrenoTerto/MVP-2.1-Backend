package com.redepatas.api.dtos.PayloadsDto;

public record CreditCardPayload(
    String creditCardNumber,
    String creditCardBrand,
    String creditCardToken
) {}