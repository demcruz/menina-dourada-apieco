package br.com.ecommerce.meninadourada.model;

/**
 * Enumeration for the possible statuses of an Order.
 */
public enum OrderStatus {
    PENDING,        // Order created, awaiting payment
    PAID,           // Payment approved
    PROCESSING,     // Order in processing (picking, packing)
    SHIPPED,        // Order shipped
    DELIVERED,      // Order delivered
    CANCELLED,      // Order cancelled
    REFUNDED,       // Order refunded
    REJECTED        // Payment rejected
}
