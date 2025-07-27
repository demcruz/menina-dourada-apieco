package br.com.ecommerce.meninadourada.dto;

import jakarta.validation.constraints.Email; // Importe para validação de e-mail
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class EmailSubscriptionRequestDTO {


    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido") // Valida o formato do e-mail
    @Size(max = 255, message = "O e-mail não pode ter mais de 255 caracteres")
    private String email;

    // Construtor padrão
    public EmailSubscriptionRequestDTO() {
    }

    // Construtor com todos os argumentos
    public EmailSubscriptionRequestDTO(String email) {
        this.email = email;
    }

    // Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Método toString
    @Override
    public String toString() {
        return "EmailSubscriptionRequestDTO{" +
                "email='" + email + '\'' +
                '}';
    }

    // Métodos equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailSubscriptionRequestDTO that = (EmailSubscriptionRequestDTO) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
