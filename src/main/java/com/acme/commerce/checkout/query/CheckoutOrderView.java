package com.acme.commerce.checkout.query;

import com.acme.commerce.checkout.domain.CheckoutOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "checkout_order_view")
public class CheckoutOrderView {
    @Id
    @Column(name = "checkout_id")
    private UUID checkoutId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CheckoutOrderView() {
    }

    public CheckoutOrderView(CheckoutOrder order) {
        checkoutId = order.getCheckoutId();
        sku = order.getSku();
        quantity = order.getQuantity();
        status = order.getStatus().name();
        updatedAt = Instant.now();
    }

    public UUID getCheckoutId() { return checkoutId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
}