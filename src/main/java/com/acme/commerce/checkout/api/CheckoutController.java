package com.acme.commerce.checkout.api;

import com.acme.commerce.checkout.query.CheckoutOrderView;
import com.acme.commerce.checkout.query.CheckoutOrderViewRepository;
import com.acme.commerce.checkout.saga.CheckoutCoordinator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {
    private final CheckoutCoordinator coordinator;
    private final CheckoutOrderViewRepository viewRepository;

    public CheckoutController(CheckoutCoordinator coordinator, CheckoutOrderViewRepository viewRepository) {
        this.coordinator = coordinator;
        this.viewRepository = viewRepository;
    }

    @PostMapping
    public CheckoutResponse create(@Valid @RequestBody CheckoutRequest request) {
        return CheckoutResponse.from(coordinator.place(request.checkoutId(), request.sku(), request.quantity()));
    }

    @GetMapping("/{checkoutId}")
    public CheckoutOrderView get(@PathVariable UUID checkoutId) {
        return viewRepository.findById(checkoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checkout not found"));
    }
}