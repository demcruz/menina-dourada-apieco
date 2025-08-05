// src/main/java/br/com/ecommerce/meninadourada/controller/PaymentController.java
package br.com.ecommerce.meninadourada.controller;

import br.com.ecommerce.meninadourada.dto.PaymentRequestDTO;
import br.com.ecommerce.meninadourada.dto.PaymentStatusUpdateRequestDTO;
import br.com.ecommerce.meninadourada.dto.PreferenceResponseDTO;
import br.com.ecommerce.meninadourada.service.MercadoPagoService;
import br.com.ecommerce.meninadourada.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> createPreference(@Valid @RequestBody PaymentRequestDTO dto) {
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
            @RequestParam(value = "id",    required = false) String idParam,
            @RequestParam(value = "topic", required = false) String topicParam,
            @RequestBody(required = false) Map<String, Object> payload) {

        // 1) Logue tudo pra inspeção
        logger.info("📬 Webhook recebido → query id={} topic={}  |  body payload={}",
                idParam, topicParam, payload);

        // 2) Defina id e topic, preferindo query params
        String id    = idParam;
        String topic = topicParam;

        // 3) Se não veio topic na query, tente body.topic (v2)
        if (isBlank(topic) && payload != null && payload.containsKey("topic")) {
            topic = payload.get("topic").toString();
        }
        // 4) Se ainda não veio, tente body.type (v1)
        if (isBlank(topic) && payload != null && payload.containsKey("type")) {
            topic = payload.get("type").toString();
        }

        // 5) Se não veio id na query, tente body.data.id (v1)
        if (isBlank(id) && payload != null && payload.containsKey("data")) {
            Object data = payload.get("data");
            if (data instanceof Map<?, ?> m && m.get("id") != null) {
                id = m.get("id").toString();
            }
        }
        // 6) Se ainda não veio, tente body.resource (v2)
        if (isBlank(id) && payload != null && payload.containsKey("resource")) {
            String resource = payload.get("resource").toString();
            id = resource.substring(resource.lastIndexOf('/') + 1);
        }

        // 7) Se faltou algo, retorna 400
        if (isBlank(id) || isBlank(topic)) {
            logger.warn("🔴 Parâmetros ausentes id={} topic={}", id, topic);
            return ResponseEntity
                    .badRequest()
                    .body("Faltam parâmetros obrigatórios: id e/ou topic");
        }

        // 8) Chama seu serviço
        try {
            logger.info("✅ Processando webhook com id={} topic={}", id, topic);
            mercadoPagoService.handleWebhookNotification(id, topic);
            return ResponseEntity.ok("Webhook processado");
        } catch (Exception e) {
            logger.error("❌ Erro no processamento do webhook", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
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
