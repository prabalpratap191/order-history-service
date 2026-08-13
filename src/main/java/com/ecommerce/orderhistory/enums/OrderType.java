package com.ecommerce.orderhistory.enums;

/**
 * Maps to legacy order history type IDs.
 * Legacy: Order History Types from legacy_app_spec.md Section 5.2
 */
public enum OrderType {
    NEW_ORDER(1, "New Order"),
    ORDER_UPDATE(2, "Order Update"),
    INQUIRY(3, "Inquiry"),
    RETURN_REFUND(4, "Return/Refund"),
    COMPLAINT(5, "Complaint"),
    FEEDBACK(6, "Feedback");

    private final int id;
    private final String description;

    OrderType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }

    public static OrderType fromId(int id) {
        for (OrderType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Invalid OrderType id: " + id);
    }
}