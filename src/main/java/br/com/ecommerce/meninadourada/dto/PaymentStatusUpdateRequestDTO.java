package br.com.ecommerce.meninadourada.dto;

public class PaymentStatusUpdateRequestDTO {

    private String paymentId;
    private String preferenceId;
    private String status;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PaymentStatusUpdateRequest{" +
                "paymentId='" + paymentId + '\'' +
                ", preferenceId='" + preferenceId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
