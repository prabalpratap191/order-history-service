package com.ecommerce.orderhistory.entity;

import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Maps to legacy ORDER_HISTORY table.
 * Legacy: OrderHistoryVO, ViewOrderHistoryVO
 */
@Entity
@Table(name = "order_history", indexes = {
    @Index(name = "idx_order_history_customer", columnList = "customer_id"),
    @Index(name = "idx_order_history_order_date", columnList = "order_date"),
    @Index(name = "idx_order_history_order_type", columnList = "order_type")
})
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_history_id")
    private Long orderHistoryId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_entry_type", nullable = false, length = 20)
    private OrderEntryType orderEntryType;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "representative", length = 100)
    private String representative;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private OrderType orderType;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Column(name = "external_flag", nullable = false)
    private boolean externalFlag = false;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public OrderHistory() {}

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public Long getOrderHistoryId() { return orderHistoryId; }
    public void setOrderHistoryId(Long orderHistoryId) { this.orderHistoryId = orderHistoryId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public OrderEntryType getOrderEntryType() { return orderEntryType; }
    public void setOrderEntryType(OrderEntryType orderEntryType) { this.orderEntryType = orderEntryType; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getRepresentative() { return representative; }
    public void setRepresentative(String representative) { this.representative = representative; }
    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public boolean isExternalFlag() { return externalFlag; }
    public void setExternalFlag(boolean externalFlag) { this.externalFlag = externalFlag; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
}