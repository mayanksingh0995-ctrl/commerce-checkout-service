package com.acme.commerce.checkout.saga;

import com.acme.commerce.checkout.domain.CheckoutOrder;
import com.acme.commerce.checkout.domain.CheckoutStatus;
import com.acme.commerce.contracts.inventory.v1.InventoryServiceGrpc;
import com.acme.commerce.contracts.inventory.v1.ReleaseStockRequest;
import com.acme.commerce.contracts.inventory.v1.ReservationStatus;
import com.acme.commerce.contracts.inventory.v1.ReserveStockRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CheckoutCoordinator {
    private final CheckoutTransactions transactions;
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventory;

    public CheckoutCoordinator(CheckoutTransactions transactions,
                                InventoryServiceGrpc.InventoryServiceBlockingStub inventory) {
        this.transactions = transactions;
        this.inventory = inventory;
    }

    public CheckoutOrder place(UUID checkoutId, String sku, int quantity) {
        CheckoutOrder order = transactions.begin(checkoutId, sku, quantity);
        if (order.getStatus() != CheckoutStatus.PENDING) {
            return order;
        }

        try {
            var response = inventory.withDeadlineAfter(2, TimeUnit.SECONDS).reserveStock(
                    ReserveStockRequest.newBuilder()
                            .setReservationId(order.getReservationId().toString())
                            .setCheckoutId(order.getCheckoutId().toString())
                            .setSku(order.getSku())
                            .setQuantity(order.getQuantity())
                            .build());
            if (response.getStatus() != ReservationStatus.RESERVATION_STATUS_RESERVED) {
                return transactions.transition(checkoutId, CheckoutStatus.REJECTED);
            }
        } catch (RuntimeException reserveFailure) {
            return compensate(checkoutId, order);
        }

        try {
            return transactions.confirm(checkoutId);
        } catch (RuntimeException confirmationFailure) {
            return compensate(checkoutId, order);
        }
    }

    private CheckoutOrder compensate(UUID checkoutId, CheckoutOrder order) {
        try {
            inventory.withDeadlineAfter(2, TimeUnit.SECONDS).releaseStock(
                    ReleaseStockRequest.newBuilder()
                            .setReservationId(order.getReservationId().toString())
                            .setCheckoutId(order.getCheckoutId().toString())
                            .build());
            return transactions.transition(checkoutId, CheckoutStatus.REJECTED);
        } catch (RuntimeException compensationFailure) {
            return transactions.transition(checkoutId, CheckoutStatus.COMPENSATION_REQUIRED);
        }
    }
}