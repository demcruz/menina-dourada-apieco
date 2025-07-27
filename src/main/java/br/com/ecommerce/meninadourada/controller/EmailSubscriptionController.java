package br.com.ecommerce.meninadourada.controller;

import br.com.ecommerce.meninadourada.dto.EmailSubscriptionRequestDTO;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import br.com.ecommerce.meninadourada.model.EmailSubscription;
import br.com.ecommerce.meninadourada.service.EmailSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map; // Para o Map.of no erro
@RestController
@RequestMapping("/api/newsletter") // Caminho base para os endpoints da newsletter
public class EmailSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(EmailSubscriptionController.class);

    private final EmailSubscriptionService emailSubscriptionService;

    @Autowired
    public EmailSubscriptionController(EmailSubscriptionService emailSubscriptionService) {
        this.emailSubscriptionService = emailSubscriptionService;
    }

    /**
     * Endpoint HTTP POST para cadastrar um e-mail na newsletter.
     *
     * @param dto O DTO contendo o e-mail a ser cadastrado.
     * @return ResponseEntity com a inscrição criada e status HTTP 201 (Created).
     */
    @PostMapping("/subscribe")
    public ResponseEntity<EmailSubscription> subscribeEmail(@Valid @RequestBody EmailSubscriptionRequestDTO dto) {
        logger.info("Recebida requisição para inscrever e-mail: {}", dto.getEmail());
        EmailSubscription newSubscription = emailSubscriptionService.subscribeEmail(dto);
        logger.info("E-mail {} inscrito com sucesso. ID: {}", newSubscription.getEmail(), newSubscription.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newSubscription);
    }

    /**
     * Endpoint HTTP GET para buscar uma inscrição de e-mail por ID.
     * @param id O ID da inscrição.
     * @return ResponseEntity com a inscrição encontrada e status HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailSubscription> getSubscriptionById(@PathVariable String id) {
        logger.info("Recebida requisição para buscar inscrição de e-mail com ID: {}", id);
        EmailSubscription subscription = emailSubscriptionService.getSubscriptionById(id);
        logger.info("Inscrição de e-mail com ID {} encontrada.", id);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Endpoint HTTP GET para listar todas as inscrições de e-mail.
     * @return ResponseEntity com uma lista de inscrições e status HTTP 200 (OK).
     */
    @GetMapping("/all")
    public ResponseEntity<List<EmailSubscription>> getAllSubscriptions() {
        logger.info("Recebida requisição para listar todas as inscrições de e-mail.");
        List<EmailSubscription> subscriptions = emailSubscriptionService.getAllSubscriptions();
        logger.info("Retornando {} inscrições de e-mail.", subscriptions.size());
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Endpoint HTTP DELETE para remover uma inscrição de e-mail.
     * @param id O ID da inscrição a ser removida.
     * @return ResponseEntity com status HTTP 204 (No Content) se a exclusão for bem-sucedida.
     */
    @DeleteMapping("/unsubscribe/{id}")
    public ResponseEntity<Void> unsubscribeEmail(@PathVariable String id) {
        logger.info("Recebida requisição para remover inscrição de e-mail com ID: {}", id);
        emailSubscriptionService.unsubscribeEmail(id);
        logger.info("Inscrição de e-mail com ID {} removida com sucesso.", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handler de exceção para IllegalArgumentException (usado para e-mail duplicado).
     * Retorna status HTTP 400 (Bad Request).
     * @param ex A exceção IllegalArgumentException.
     * @return ResponseEntity com a mensagem de erro e status HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Erro de requisição inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handler de exceção para ResourceNotFoundException (reutilizado para inscrições não encontradas).
     * Retorna status HTTP 404 (Not Found).
     * @param ex A exceção ResourceNotFoundException.
     * @return ResponseEntity com a mensagem de erro e status HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
