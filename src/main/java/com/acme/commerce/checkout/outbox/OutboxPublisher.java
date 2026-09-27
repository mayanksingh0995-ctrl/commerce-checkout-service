package com.acme.commerce.checkout.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {
    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
    public void publishPending() {
        for (OutboxEvent event : repository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafkaTemplate.send("commerce.checkout.events", event.getAggregateId().toString(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);
                markPublished(event);
            } catch (Exception exception) {
                logger.warn("Outbox delivery failed for event {}", event.getEventId(), exception);
                return;
            }
        }
    }

    @Transactional
    public void markPublished(OutboxEvent event) {
        repository.findById(event.getEventId()).ifPresent(pending -> {
            if (pending.getPublishedAt() == null) {
                pending.markPublished();
                repository.save(pending);
            }
        });
    }
}