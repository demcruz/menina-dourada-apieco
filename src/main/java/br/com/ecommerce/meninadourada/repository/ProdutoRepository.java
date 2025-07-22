package br.com.ecommerce.meninadourada.repository;

import br.com.ecommerce.meninadourada.model.Produto;
import org.springframework.data.mongodb.repository.MongoRepository; // Importe MongoRepository
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade Produto, utilizando Spring Data MongoDB.
 * Estende MongoRepository para obter métodos CRUD básicos automaticamente.
 */
@Repository // Indica que esta interface é um componente de repositório do Spring
public interface ProdutoRepository extends MongoRepository<Produto, String> {
    // MongoRepository já fornece: save, findById, findAll, delete, etc.

    // Método personalizado para buscar produtos por nome.
    // O Spring Data MongoDB infere a query a partir do nome do método.
    List<Produto> findByNome(String nome);
}
