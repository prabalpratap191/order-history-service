package com.ecommerce.orderhistory.enums;

/**
 * Maps to legacy OrderIdentifierType constants.
 * Legacy: com.ecommerce.business.constants.OrderIdentifierType
 */
public enum OrderEntryType {
    CUSTOMER(1, "Customer"),
    PRODUCT(2, "Product"),
    ORDER(3, "Order");

    private final int id;
    private final String description;

    OrderEntryType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }

    public static OrderEntryType fromId(int id) {
        for (OrderEntryType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Invalid OrderEntryType id: " + id);
    }
}