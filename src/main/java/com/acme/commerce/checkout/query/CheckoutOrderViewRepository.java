package com.acme.commerce.checkout.query;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckoutOrderViewRepository extends JpaRepository<CheckoutOrderView, UUID> {
}