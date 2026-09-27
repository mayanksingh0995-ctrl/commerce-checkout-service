package com.acme.commerce.checkout.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckoutOrderRepository extends JpaRepository<CheckoutOrder, UUID> {
}