// src/main/java/br/com/ecommerce/meninadourada/controller/PaymentController.java
package br.com.ecommerce.meninadourada.controller;

import br.com.ecommerce.meninadourada.dto.PaymentRequestDTO;
import br.com.ecommerce.meninadourada.dto.PreferenceResponseDTO;
import br.com.ecommerce.meninadourada.service.MercadoPagoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final MercadoPagoService mercadoPagoService;

    public PaymentController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    /**
     * POST /create-preference
     */
    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference(
            @Valid @RequestBody PaymentRequestDTO dto) {
        logger.info("Received create-preference for user: {}", dto.getUserId());

        try {
            PreferenceResponseDTO resp = mercadoPagoService.createPaymentPreference(dto);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação na requisição: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar preferência de pagamento", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao criar preferência de pagamento"));
        }
    }

    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<String> handleMercadoPagoWebhook(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "topic", required = false) String topic) {
        if (id == null || topic == null) {
            logger.warn("Webhook recebido com parâmetros inválidos. id={}, topic={}", id, topic);
            return ResponseEntity.badRequest().body("Parâmetros obrigatórios ausentes");
        }

        try {
            logger.info("Received MP webhook. id={}, topic={}", id, topic);
            mercadoPagoService.handleWebhookNotification(id, topic);
            return ResponseEntity.ok("Webhook processado");
        } catch (Exception e) {
            logger.error("Erro ao processar webhook do Mercado Pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar webhook");
        }
    }
}
