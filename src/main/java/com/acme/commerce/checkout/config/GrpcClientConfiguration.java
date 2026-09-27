package com.acme.commerce.checkout.config;

import com.acme.commerce.contracts.inventory.v1.InventoryServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfiguration {
    @Bean
    InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub(GrpcChannelFactory channels) {
        return InventoryServiceGrpc.newBlockingStub(channels.createChannel("inventory"));
    }
}