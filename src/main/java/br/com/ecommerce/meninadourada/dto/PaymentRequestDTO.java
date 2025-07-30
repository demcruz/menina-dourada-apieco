package br.com.ecommerce.meninadourada.dto;

import br.com.ecommerce.meninadourada.model.ShippingAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PaymentRequestDTO {

    @NotBlank(message = "The user ID is mandatory")
    private String userId;

    @NotNull(message = "At least one order item is mandatory")
    private List<OrderItemDTO> items; // DTOs of order items

    @NotNull(message = "The total amount is mandatory")
    @Positive(message = "The total amount must be positive")
    private BigDecimal totalAmount;

    // Payer information (simplificado para o exemplo)
    @NotBlank(message = "The payer's email is mandatory")
    @Email(message = "Invalid email format")
    private String payerEmail;

    // Dados do Cliente
    @NotBlank(message = "Customer name is mandatory")
    private String customerName;
    @NotBlank(message = "Customer phone is mandatory")
    private String customerPhone;
    @NotBlank(message = "Customer CPF is mandatory") // NOVO: CPF do cliente
    @Size(min = 11, max = 14, message = "CPF must have between 11 and 14 characters") // Validação básica de tamanho
    private String customerCpf;

    // Endereço de Entrega
    @NotNull(message = "Shipping address is mandatory")
    @Valid // Garante que as validações dentro de ShippingAddress sejam aplicadas
    private ShippingAddress shippingAddress;

    // Default constructor
    public PaymentRequestDTO() {
    }

    // Constructor with all arguments (updated to include new fields)
    public PaymentRequestDTO(String userId, List<OrderItemDTO> items, BigDecimal totalAmount, String payerEmail, String customerName, String customerPhone, String customerCpf, ShippingAddress shippingAddress) {
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.payerEmail = payerEmail;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerCpf = customerCpf; // NOVO: Atribuição no construtor
        this.shippingAddress = shippingAddress;
    }

    // Getters and Setters (updated for new fields)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPayerEmail() { return payerEmail; }
    public void setPayerEmail(String payerEmail) { this.payerEmail = payerEmail; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerCpf() { return customerCpf; } // NOVO: Getter
    public void setCustomerCpf(String customerCpf) { this.customerCpf = customerCpf; } // NOVO: Setter
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }

    @Override
    public String toString() {
        return "PaymentRequestDTO{" +
                "userId='" + userId + '\'' +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                ", payerEmail='" + payerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", customerCpf='" + customerCpf + '\'' + // NOVO: Incluir no toString
                ", shippingAddress=" + shippingAddress +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRequestDTO that = (PaymentRequestDTO) o;
        return Objects.equals(userId, that.userId) && Objects.equals(items, that.items) && Objects.equals(totalAmount, that.totalAmount) && Objects.equals(payerEmail, that.payerEmail) && Objects.equals(customerName, that.customerName) && Objects.equals(customerPhone, that.customerPhone) && Objects.equals(customerCpf, that.customerCpf) && Objects.equals(shippingAddress, that.shippingAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, items, totalAmount, payerEmail, customerName, customerPhone, customerCpf, shippingAddress);
    }
}
