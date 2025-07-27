package br.com.ecommerce.meninadourada.repository;

import br.com.ecommerce.meninadourada.model.EmailSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface EmailSubscriptionRepository extends MongoRepository<EmailSubscription, String>{

    Optional<EmailSubscription> findByEmail(String email);
}
