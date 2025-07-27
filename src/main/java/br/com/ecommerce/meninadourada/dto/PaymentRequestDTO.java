package br.com.ecommerce.meninadourada.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class PaymentRequestDTO {

    @NotBlank
    private String userId;

    @NotBlank @Email
    private String payerEmail;

    @NotNull
    private BigDecimal totalAmount;

    @NotNull @Size(min = 1)
    private List<OrderItemDTO> items;

    public PaymentRequestDTO() {
    }

    public PaymentRequestDTO(String userId, String payerEmail, BigDecimal totalAmount, List<OrderItemDTO> items) {
        this.userId = userId;
        this.payerEmail = payerEmail;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
