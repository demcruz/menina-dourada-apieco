package br.com.ecommerce.meninadourada.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed; // Importe para criar índice único
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime; // Para registrar a data de cadastro
import java.util.Objects;

@Document(collection = "newsletter_subscriptions") // Nome da coleção no MongoDB
public class EmailSubscription {

    // O ID da inscrição. Mapeia para o campo _id do MongoDB.
    @Id
    private String id;

    // O endereço de e-mail do assinante.
    // @Indexed(unique = true) garante que não haverá e-mails duplicados na coleção.
    @Indexed(unique = true)
    @Field("email")
    private String email;

    // Data e hora em que a inscrição foi realizada.
    @Field("subscribedAt")
    private LocalDateTime subscribedAt;

    // Construtor padrão
    public EmailSubscription() {
        this.subscribedAt = LocalDateTime.now(); // Define a data de inscrição automaticamente
    }

    // Construtor com todos os argumentos
    public EmailSubscription(String id, String email, LocalDateTime subscribedAt) {
        this.id = id;
        this.email = email;
        this.subscribedAt = subscribedAt;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    // Método toString
    @Override
    public String toString() {
        return "EmailSubscription{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", subscribedAt=" + subscribedAt +
                '}';
    }

    // Métodos equals e hashCode (baseados no ID e email para unicidade)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailSubscription that = (EmailSubscription) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
