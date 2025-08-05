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
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "topic", required = false) String topicParam,
            @RequestBody(required = false) Map<String, Object> payload) {

        // 1) Logue tudo pra ver o que realmente chegou
        logger.info("📬 Webhook recebido → query id={}, topic={}  |  body payload={}",
                idParam, topicParam, payload);

        // 2) Defina id e topic, preferindo query params
        String id = idParam;
        String topic = topicParam;

        // 3) Se não vier topic na query, pegue do JSON ("topic")
        if ((topic == null || topic.isBlank()) && payload != null && payload.containsKey("topic")) {
            topic = payload.get("topic").toString();
        }

        // 4) Se não vier id na query, extraia de payload.data.id (v1) ou de payload.resource (v2)
        if ((id == null || id.isBlank()) && payload != null) {
            if (payload.containsKey("data") && payload.get("data") instanceof Map) {
                Object o = ((Map<?,?>)payload.get("data")).get("id");
                if (o != null) id = o.toString();
            } else if (payload.containsKey("resource")) {
                String resource = payload.get("resource").toString();
                // extrai tudo depois da última barra
                id = resource.substring(resource.lastIndexOf('/') + 1);
            }
        }

        // 5) Se ainda faltar algo, devolva 400
        if (id == null || topic == null) {
            logger.warn("🔴 Parâmetros obrigatórios ausentes. id={} topic={}", id, topic);
            return ResponseEntity.badRequest()
                    .body("Parâmetros obrigatórios ausentes: id e topic");
        }

        try {
            logger.info("✅ Chamando serviço de webhook com id={} topic={}", id, topic);
            mercadoPagoService.handleWebhookNotification(id, topic);
            return ResponseEntity.ok("Webhook processado");
        } catch (Exception e) {
            logger.error("❌ Erro ao processar webhook:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno");
        }
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
