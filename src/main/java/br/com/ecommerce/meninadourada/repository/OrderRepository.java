package br.com.ecommerce.meninadourada.repository;


import br.com.ecommerce.meninadourada.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Importe Optional


/**
 * Repositório para a entidade Order, utilizando Spring Data MongoDB.
 * Estende MongoRepository para obter métodos CRUD básicos automaticamente.
 * O primeiro parâmetro é a entidade (Order) e o segundo é o tipo do ID (String).
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    /**
     * Busca um pedido pelo ID de pagamento (ID da Preferência ou Order ID do Mercado Pago).
     * @param paymentId O ID de pagamento do Mercado Pago.
     * @return Um Optional contendo o Pedido, se encontrado, ou vazio.
     */
    Optional<Order> findByPaymentId(String paymentId);

    /**
     * Busca um pedido pela sua referência externa (external_reference) do Mercado Pago.
     * @param externalReference A referência externa do Mercado Pago.
     * @return Um Optional contendo o Pedido, se encontrado, ou vazio.
     */
    Optional<Order> findByExternalReference(String externalReference); // NOVO: Método para buscar pela externalReference
}