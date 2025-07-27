// src/main/java/br/com/ecommerce/meninadourada/controller/PaymentController.java
package br.com.ecommerce.meninadourada.controller;

import br.com.ecommerce.meninadourada.dto.PaymentRequestDTO;
import br.com.ecommerce.meninadourada.dto.PreferenceResponseDTO;
import br.com.ecommerce.meninadourada.service.MercadoPagoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PreferenceResponseDTO> createPreference(
            @Valid @RequestBody PaymentRequestDTO dto) {
        logger.info("Received create-preference for user: {}", dto.getUserId());
        PreferenceResponseDTO resp = mercadoPagoService.createPaymentPreference(dto);
        return ResponseEntity.ok(resp);
    }

    /**
     * POST /webhook/mercadopago
     */
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoWebhook(
            @RequestParam("id") String id,
            @RequestParam("topic") String topic) {
        logger.info("Received MP webhook. id={}, topic={}", id, topic);
        mercadoPagoService.handleWebhookNotification(id, topic);
        return ResponseEntity.ok().build();
    }
}
