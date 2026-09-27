package com.acme.commerce.checkout.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckoutRequest(
        @NotNull UUID checkoutId,
        @NotBlank String sku,
        @Min(1) int quantity) {
}