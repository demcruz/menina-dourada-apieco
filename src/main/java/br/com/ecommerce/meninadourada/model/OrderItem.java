package br.com.ecommerce.meninadourada.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {

    @Field("productId")
    private String productId; // Product ID

    @Field("productName")
    private String productName; // Product name (for historical purposes)

    @Field("variationId")
    private String variationId; // Specific variation ID (color, size)

    @Field("quantity")
    private Integer quantity; // Item quantity

    @Field("unitPrice")
    private BigDecimal unitPrice; // Unit price at the time of purchase

    // Default constructor
    public OrderItem() {
    }

    // Constructor with all arguments
    public OrderItem(String productId, String productName, String variationId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.variationId = variationId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getVariationId() { return variationId; }
    public void setVariationId(String variationId) { this.variationId = variationId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", variationId='" + variationId + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(productId, orderItem.productId) && Objects.equals(variationId, orderItem.variationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, variationId);
    }
}
