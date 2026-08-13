package com.ecommerce.orderhistory.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
public class OrderHistoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderHistoryCreatedTopic;
    private final String orderHistoryUpdatedTopic;

    public OrderHistoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                      @Value("${order-history.kafka.topic.order-history-created}") String orderHistoryCreatedTopic,
                                      @Value("${order-history.kafka.topic.order-history-updated}") String orderHistoryUpdatedTopic) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.orderHistoryCreatedTopic = Objects.requireNonNull(orderHistoryCreatedTopic);
        this.orderHistoryUpdatedTopic = Objects.requireNonNull(orderHistoryUpdatedTopic);
    }

    public void publishOrderHistoryCreated(OrderHistoryCreatedEvent event) {
        log.info("Publishing OrderHistoryCreatedEvent for order history ID: {}", event.orderHistoryId());
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(orderHistoryCreatedTopic, String.valueOf(event.orderHistoryId()), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderHistoryCreatedEvent for ID: {}", event.orderHistoryId(), ex);
            } else {
                log.debug("OrderHistoryCreatedEvent published successfully for ID: {}, partition: {}, offset: {}",
                    event.orderHistoryId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }

    public void publishOrderHistoryUpdated(OrderHistoryUpdatedEvent event) {
        log.info("Publishing OrderHistoryUpdatedEvent for order history ID: {}", event.orderHistoryId());
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(orderHistoryUpdatedTopic, String.valueOf(event.orderHistoryId()), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderHistoryUpdatedEvent for ID: {}", event.orderHistoryId(), ex);
            } else {
                log.debug("OrderHistoryUpdatedEvent published successfully for ID: {}, partition: {}, offset: {}",
                    event.orderHistoryId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}