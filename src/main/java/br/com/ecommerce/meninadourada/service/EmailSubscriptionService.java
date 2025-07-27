package br.com.ecommerce.meninadourada.service;



import br.com.ecommerce.meninadourada.dto.EmailSubscriptionRequestDTO;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import br.com.ecommerce.meninadourada.model.EmailSubscription;
import br.com.ecommerce.meninadourada.repository.EmailSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException; // Importe para lidar com e-mail duplicado
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bson.types.ObjectId; // Para gerar IDs se necessário, embora o MongoRepository faça isso

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class EmailSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSubscriptionService.class);

    private final EmailSubscriptionRepository emailSubscriptionRepository;

    @Autowired
    public EmailSubscriptionService(EmailSubscriptionRepository emailSubscriptionRepository) {
        this.emailSubscriptionRepository = emailSubscriptionRepository;
    }

    /**
     * Cadastra um novo e-mail para a newsletter.
     * Verifica se o e-mail já existe para evitar duplicatas.
     *
     * @param dto O DTO contendo o e-mail a ser cadastrado.
     * @return A inscrição de e-mail salva.
     * @throws IllegalArgumentException Se o e-mail já estiver cadastrado.
     */
    public EmailSubscription subscribeEmail(EmailSubscriptionRequestDTO dto) {
        logger.info("Tentativa de inscrição de e-mail: {}", dto.getEmail());

        // Verifica se o e-mail já existe
        Optional<EmailSubscription> existingSubscription = emailSubscriptionRepository.findByEmail(dto.getEmail());
        if (existingSubscription.isPresent()) {
            logger.warn("E-mail {} já cadastrado para newsletter.", dto.getEmail());
            throw new IllegalArgumentException("E-mail já cadastrado para a newsletter.");
        }

        EmailSubscription subscription = new EmailSubscription();
        subscription.setId(new ObjectId().toHexString()); // Gera um ID único
        subscription.setEmail(dto.getEmail());
        subscription.setSubscribedAt(LocalDateTime.now()); // Garante a data atual

        try {
            EmailSubscription savedSubscription = emailSubscriptionRepository.save(subscription);
            logger.info("E-mail {} cadastrado na newsletter com sucesso. ID: {}", savedSubscription.getEmail(), savedSubscription.getId());
            return savedSubscription;
        } catch (DuplicateKeyException e) {
            // Caso raro de corrida onde findByEmail não pegou, mas o índice único do MongoDB sim.
            logger.warn("E-mail {} já cadastrado devido a DuplicateKeyException.", dto.getEmail());
            throw new IllegalArgumentException("E-mail já cadastrado para a newsletter.");
        } catch (Exception e) {
            logger.error("Erro ao cadastrar e-mail {} na newsletter: {}", dto.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Falha ao cadastrar e-mail na newsletter: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma inscrição de e-mail pelo seu ID.
     * @param id O ID da inscrição.
     * @return A inscrição encontrada.
     * @throws ResourceNotFoundException Se a inscrição não for encontrada.
     */
    public EmailSubscription getSubscriptionById(String id) {
        logger.info("Buscando inscrição de e-mail com ID: {}", id);
        return emailSubscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Inscrição de e-mail com ID {} não encontrada.", id);
                    return new ResourceNotFoundException("Inscrição de e-mail não encontrada com ID: " + id);
                });
    }

    /**
     * Lista todas as inscrições de e-mail.
     * @return Uma lista de todas as inscrições.
     */
    public List<EmailSubscription> getAllSubscriptions() {
        logger.info("Listando todas as inscrições de e-mail.");
        return emailSubscriptionRepository.findAll();
    }

    /**
     * Deleta uma inscrição de e-mail pelo seu ID.
     * @param id O ID da inscrição a ser deletada.
     * @throws ResourceNotFoundException Se a inscrição não for encontrada.
     */
    public void unsubscribeEmail(String id) {
        logger.info("Iniciando exclusão de inscrição de e-mail com ID: {}", id);
        EmailSubscription subscriptionToDelete = emailSubscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Tentativa de deletar inscrição de e-mail inexistente. ID: {}", id);
                    return new ResourceNotFoundException("Inscrição de e-mail não encontrada com ID: " + id);
                });
        emailSubscriptionRepository.delete(subscriptionToDelete);
        logger.info("Inscrição de e-mail com ID {} deletada com sucesso.", id);
    }
}
