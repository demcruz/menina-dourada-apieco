package br.com.ecommerce.meninadourada.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Order in MongoDB.
 * Contains information about the customer, order items, status, and payment details.
 */
@Document(collection = "orders") // Collection name in MongoDB
public class Order {

    @Id
    private String id;

    @Field("userId")
    private String userId; // ID of the user who placed the order

    @Field("orderDate")
    private LocalDateTime orderDate; // Date and time of the order

    @Field("items")
    private List<OrderItem> items = new ArrayList<>(); // Order items

    @Field("totalAmount")
    private BigDecimal totalAmount; // Total amount of the order

    @Field("status")
    private OrderStatus status; // Order status (PENDING, PAID, SHIPPED, etc.)

    @Field("paymentId")
    private String paymentId; // Payment transaction ID (e.g., Mercado Pago Preference ID or Order ID)

    @Field("paymentStatus")
    private String paymentStatus; // Payment status (PENDING, APPROVED, REJECTED, etc.)

    @Field("externalReference") // NOVO: Campo para a external_reference do Mercado Pago
    private String externalReference;

    // Default constructor
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING; // Initial status
    }

    // Constructor with all arguments (incluindo externalReference)
    public Order(String id, String userId, LocalDateTime orderDate, List<OrderItem> items, BigDecimal totalAmount, OrderStatus status, String paymentId, String paymentStatus, String externalReference) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.externalReference = externalReference;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getExternalReference() { return externalReference; } // NOVO: Getter para externalReference
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; } // NOVO: Setter para externalReference

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", orderDate=" + orderDate +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", paymentId='" + paymentId + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", externalReference='" + externalReference + '\'' + // NOVO: Incluir no toString
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
