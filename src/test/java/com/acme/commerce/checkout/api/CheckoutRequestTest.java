package com.acme.commerce.checkout.api;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutRequestTest {
    @Test
    void rejectsNonPositiveQuantity() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var violations = validatorFactory.getValidator().validate(
                    new CheckoutRequest(UUID.randomUUID(), "DEMO-SKU-001", 0));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("quantity");
        }
    }
}