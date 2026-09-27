package com.acme.commerce.checkout.saga;

import com.acme.commerce.checkout.domain.CheckoutOrder;
import com.acme.commerce.checkout.domain.CheckoutOrderRepository;
import com.acme.commerce.checkout.domain.CheckoutStatus;
import com.acme.commerce.checkout.outbox.OutboxEvent;
import com.acme.commerce.checkout.outbox.OutboxEventRepository;
import com.acme.commerce.checkout.query.CheckoutOrderView;
import com.acme.commerce.checkout.query.CheckoutOrderViewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CheckoutTransactions {
    private final CheckoutOrderRepository orderRepository;
    private final CheckoutOrderViewRepository viewRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CheckoutTransactions(CheckoutOrderRepository orderRepository,
                                CheckoutOrderViewRepository viewRepository,
                                OutboxEventRepository outboxRepository,
                                ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.viewRepository = viewRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CheckoutOrder begin(UUID checkoutId, String sku, int quantity) {
        var existing = orderRepository.findById(checkoutId);
        if (existing.isPresent()) {
            CheckoutOrder order = existing.get();
            if (!order.getSku().equals(sku) || order.getQuantity() != quantity) {
                throw new IllegalArgumentException("Idempotency key was already used for a different checkout request");
            }
            return order;
        }

        CheckoutOrder order = orderRepository.save(new CheckoutOrder(checkoutId, UUID.randomUUID(), sku, quantity));
        viewRepository.save(new CheckoutOrderView(order));
        return order;
    }

    @Transactional
    public CheckoutOrder transition(UUID checkoutId, CheckoutStatus status) {
        CheckoutOrder order = orderRepository.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found"));
        order.transitionTo(status);
        viewRepository.save(new CheckoutOrderView(order));
        return order;
    }

    @Transactional
    public CheckoutOrder confirm(UUID checkoutId) {
        CheckoutOrder order = orderRepository.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found"));
        order.transitionTo(CheckoutStatus.CONFIRMED);
        viewRepository.save(new CheckoutOrderView(order));
        try {
            CheckoutConfirmed event = new CheckoutConfirmed(UUID.randomUUID(), order.getCheckoutId(),
                order.getSku(), order.getQuantity(), Instant.now());
            outboxRepository.save(new OutboxEvent(event.eventId(), order.getCheckoutId(),
                "checkout.confirmed.v1", objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize checkout event", exception);
        }
        return order;
    }

    public record CheckoutConfirmed(UUID eventId, UUID checkoutId, String sku, int quantity, Instant occurredAt) {
    }
}