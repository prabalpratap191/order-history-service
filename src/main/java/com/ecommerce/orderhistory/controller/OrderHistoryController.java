package com.ecommerce.orderhistory.controller;

import com.ecommerce.orderhistory.dto.*;
import com.ecommerce.orderhistory.service.OrderHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/order-history")
@Tag(name = "Order History", description = "Order History Management APIs - Legacy: OrderHistoryDelegate, OrderHistoryAddCommand, OrderHistoryMaintainCommand, OrderHistoryEditCommand")
public class OrderHistoryController {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryController.class);
    private final OrderHistoryService orderHistoryService;

    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = Objects.requireNonNull(orderHistoryService);
    }

    @PostMapping
    @Operation(summary = "Create a new order history entry",
        description = "Legacy: OrderHistoryAddCommand.perform() + OrderHistorySaveAddCommand.perform() -> OrderHistoryDelegate.addOrderHistoryEntry(OrderHistoryVO). "
            + "Creates a new order history entry for a customer interaction. Validates order date is not in future, note length 10-4000 chars.")
    public ResponseEntity<OrderHistoryDTO> createOrderHistory(
            @Valid @RequestBody CreateOrderHistoryRequest request) {
        log.info("POST /api/v1/order-history - Creating entry for customer: {}, type: {}",
            request.customerId(), request.orderType());
        OrderHistoryDTO created = orderHistoryService.createOrderHistory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get order history for a customer",
        description = "Legacy: OrderHistoryMaintainCommand.perform() -> OrderHistoryDelegate.getCustomerOrderHistory(long, boolean). "
            + "Retrieves all order history entries for a customer in reverse chronological order. "
            + "Use includeExternal=true to include entries from external systems.")
    public ResponseEntity<ViewOrderHistoryDTO> getCustomerOrderHistory(
            @PathVariable Long customerId,
            @Parameter(description = "Include external order history entries") @RequestParam(defaultValue = "false") boolean includeExternal) {
        log.info("GET /api/v1/order-history/customer/{} - includeExternal: {}", customerId, includeExternal);
        return ResponseEntity.ok(orderHistoryService.getCustomerOrderHistory(customerId, includeExternal));
    }

    @GetMapping("/{orderHistoryId}")
    @Operation(summary = "Get a specific order history entry",
        description = "Legacy: OrderHistoryEditCommand.perform() -> OrderHistoryDelegate.getOrderHistoryEntry(long). "
            + "Retrieves a single order history entry by its ID for viewing or editing.")
    public ResponseEntity<OrderHistoryDTO> getOrderHistoryById(@PathVariable Long orderHistoryId) {
        log.info("GET /api/v1/order-history/{}", orderHistoryId);
        return ResponseEntity.ok(orderHistoryService.getOrderHistoryById(orderHistoryId));
    }

    @PutMapping("/{orderHistoryId}")
    @Operation(summary = "Update an order history entry",
        description = "Legacy: OrderHistoryEditSaveCommand.perform() -> OrderHistoryDelegate.updateOrderHistoryEntry(OrderHistoryVO). "
            + "Updates an existing order history entry. Validates order date is not in future, note length 10-4000 chars.")
    public ResponseEntity<OrderHistoryDTO> updateOrderHistory(
            @PathVariable Long orderHistoryId,
            @Valid @RequestBody UpdateOrderHistoryRequest request) {
        log.info("PUT /api/v1/order-history/{} - Updating entry", orderHistoryId);
        return ResponseEntity.ok(orderHistoryService.updateOrderHistory(orderHistoryId, request));
    }
}