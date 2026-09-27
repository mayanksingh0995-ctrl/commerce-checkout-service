package com.acme.commerce.checkout.api;

import com.acme.commerce.checkout.domain.CheckoutOrder;

import java.util.UUID;

public record CheckoutResponse(UUID checkoutId, String sku, int quantity, String status) {
    public static CheckoutResponse from(CheckoutOrder order) {
        return new CheckoutResponse(order.getCheckoutId(), order.getSku(), order.getQuantity(),
                order.getStatus().name());
    }
}