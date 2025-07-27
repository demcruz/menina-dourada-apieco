package br.com.ecommerce.meninadourada.repository;


import br.com.ecommerce.meninadourada.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 * Repositório para a entidade Order, utilizando Spring Data MongoDB.
 * Estende MongoRepository para obter métodos CRUD básicos automaticamente.
 * O primeiro parâmetro é a entidade (Order) e o segundo é o tipo do ID (String).
 */
@Repository // Indica que esta interface é um componente de repositório do Spring
public interface OrderRepository extends MongoRepository<Order, String> {
    // MongoRepository já fornece os seguintes métodos automaticamente:
    // save(entity) - Salva uma entidade
    // findById(id) - Busca uma entidade pelo ID
    // findAll() - Lista todas as entidades
    // delete(entity) - Deleta uma entidade
    // deleteAll() - Deleta todas as entidades
    // count() - Conta o número de entidades
    // E muitos outros...

    // Você pode adicionar métodos de busca personalizados aqui se precisar de queries específicas.
    // O Spring Data MongoDB pode inferir a query a partir do nome do método.
    // Exemplo:
    // List<Order> findByUserId(String userId);
    // List<Order> findByStatus(OrderStatus status);
}
