package com.acme.commerce.checkout.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "checkout_order")
public class CheckoutOrder {
    @Id
    @Column(name = "checkout_id")
    private UUID checkoutId;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckoutStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CheckoutOrder() {
    }

    public CheckoutOrder(UUID checkoutId, UUID reservationId, String sku, int quantity) {
        this.checkoutId = checkoutId;
        this.reservationId = reservationId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = CheckoutStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public UUID getCheckoutId() { return checkoutId; }
    public UUID getReservationId() { return reservationId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public CheckoutStatus getStatus() { return status; }

    public void transitionTo(CheckoutStatus next) {
        status = next;
        updatedAt = Instant.now();
    }
}