package com.ecommerce.orderhistory.event;

import com.ecommerce.orderhistory.dto.CreateOrderHistoryRequest;
import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import com.ecommerce.orderhistory.service.OrderHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Consumes order events from Order Service Kafka topics.
 * When an order is created or updated, an order history entry is automatically created.
 * Legacy: OrderHistoryDelegate.addOrderHistoryEntry() was called after order operations.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final OrderHistoryService orderHistoryService;

    public OrderEventConsumer(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = Objects.requireNonNull(orderHistoryService);
    }

    @KafkaListener(topics = "${order-history.kafka.consumer.topic.order-created}", groupId = "order-history-service-group")
    public void handleOrderCreated(Map<String, Object> event) {
        log.info("Received OrderCreatedEvent: {}", event);
        try {
            Long customerId = ((Number) event.get("customerId")).longValue();
            String orderNumber = (String) event.get("orderNumber");
            LocalDateTime orderDate = LocalDateTime.now();

            CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
                customerId,
                OrderEntryType.ORDER,
                orderDate,
                "SYSTEM",
                OrderType.NEW_ORDER,
                "Order created automatically. Order number: " + orderNumber,
                orderNumber,
                false
            );

            orderHistoryService.createOrderHistory(request);
            log.info("Order history entry created for order: {}", orderNumber);
        } catch (Exception ex) {
            log.error("Failed to process OrderCreatedEvent: {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = "${order-history.kafka.consumer.topic.order-updated}", groupId = "order-history-service-group")
    public void handleOrderUpdated(Map<String, Object> event) {
        log.info("Received OrderUpdatedEvent: {}", event);
        try {
            Long customerId = ((Number) event.get("customerId")).longValue();
            String orderNumber = (String) event.get("orderNumber");
            String previousStatus = (String) event.get("previousStatus");
            String newStatus = (String) event.get("newStatus");
            LocalDateTime orderDate = LocalDateTime.now();

            CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
                customerId,
                OrderEntryType.ORDER,
                orderDate,
                "SYSTEM",
                OrderType.ORDER_UPDATE,
                String.format("Order status updated from %s to %s. Order number: %s", previousStatus, newStatus, orderNumber),
                orderNumber,
                false
            );

            orderHistoryService.createOrderHistory(request);
            log.info("Order history entry created for order update: {} ({} -> {})", orderNumber, previousStatus, newStatus);
        } catch (Exception ex) {
            log.error("Failed to process OrderUpdatedEvent: {}", ex.getMessage(), ex);
        }
    }
}