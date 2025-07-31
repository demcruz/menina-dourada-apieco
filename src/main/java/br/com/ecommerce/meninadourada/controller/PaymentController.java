// src/main/java/br/com/ecommerce/meninadourada/controller/PaymentController.java
package br.com.ecommerce.meninadourada.controller;

import br.com.ecommerce.meninadourada.dto.PaymentRequestDTO;
import br.com.ecommerce.meninadourada.dto.PaymentStatusUpdateRequestDTO;
import br.com.ecommerce.meninadourada.dto.PreferenceResponseDTO;
import br.com.ecommerce.meninadourada.service.MercadoPagoService;
import br.com.ecommerce.meninadourada.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final MercadoPagoService mercadoPagoService;
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private OrderService orderService;


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
    public ResponseEntity<String> handleMercadoPagoWebhook(HttpServletRequest request) {
        String id = null;
        String topic = null;

        // 1. Logar query params
        String queryString = request.getQueryString();
        logger.info("=== Webhook recebido do Mercado Pago ===");
        logger.info("Query string: {}", queryString);

        // Extraindo id e topic da query string se estiverem lá
        id = request.getParameter("id");
        topic = request.getParameter("topic");
        logger.info("Parâmetros (query): id={}, topic={}", id, topic);

        // 2. Logar headers
        var headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader));
        logger.info("Headers recebidos: {}", headers);

        // 3. Ler corpo bruto
        String body;
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            body = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            logger.error("Erro lendo corpo do webhook: {}", e.getMessage(), e);
            body = null;
        }

        if (body == null || body.isBlank()) {
            logger.warn("Corpo do webhook está vazio ou não foi recebido.");
        } else {
            logger.info("Corpo bruto do webhook: {}", body);

            // Tentativa de extração fallback de id/topic do body JSON
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var root = mapper.readValue(body, java.util.Map.class);

                if ((id == null || topic == null) && root != null) {
                    if (root.containsKey("type") && topic == null) {
                        topic = String.valueOf(root.get("type"));
                    }
                    if (root.containsKey("data")) {
                        Object data = root.get("data");
                        if (data instanceof java.util.Map<?, ?> dataMap) {
                            if (id == null && dataMap.get("id") != null) {
                                id = String.valueOf(dataMap.get("id"));
                            }
                        }
                    }
                    if (root.containsKey("topic") && topic == null) {
                        topic = String.valueOf(root.get("topic"));
                    }
                    if (root.containsKey("id") && id == null) {
                        id = String.valueOf(root.get("id"));
                    }
                }
                logger.info("Parâmetros após fallback via body: id={}, topic={}", id, topic);
            } catch (Exception e) {
                logger.debug("Não foi possível parsear o body JSON para fallback de id/topic: {}", e.getMessage());
            }
        }

        // 4. Se não chegou nada útil, responde informando
        if ((id == null || topic == null) && (body == null || body.isBlank())) {
            logger.warn("Webhook sem dados suficientes: id={} topic={} body ausente", id, topic);
            return ResponseEntity.badRequest().body("Nenhum dado recebido do Mercado Pago");
        }

        // Aqui você pode continuar com o processamento real:
        // ex: mercadoPagoService.handleWebhookNotification(id, topic);

        return ResponseEntity.ok("Recebido webhook. id=" + id + ", topic=" + topic);
    }


    @PostMapping("/update")
    public ResponseEntity<String> updatePaymentStatus(@RequestBody PaymentStatusUpdateRequestDTO request) {
        log.info("🔄 Recebendo atualização de pagamento: {}", request);

        boolean updated = orderService.updateOrderAfterPayment(
                request.getPreferenceId(),
                request.getPaymentId(),
                request.getStatus()
        );

        if (updated) {
            return ResponseEntity.ok("Pedido atualizado com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido não encontrado.");
        }
    }
}
