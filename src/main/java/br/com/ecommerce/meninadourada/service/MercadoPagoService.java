// src/main/java/br/com/ecommerce/meninadourada/service/MercadoPagoService.java
package br.com.ecommerce.meninadourada.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import br.com.ecommerce.meninadourada.dto.PaymentRequestDTO;
import br.com.ecommerce.meninadourada.dto.PreferenceResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Importe suas classes de modelo e repositório para salvar o pedido
import br.com.ecommerce.meninadourada.model.Order;
import br.com.ecommerce.meninadourada.model.OrderStatus;
import br.com.ecommerce.meninadourada.repository.OrderRepository;
import org.bson.types.ObjectId; // Para gerar IDs do MongoDB

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final OrderRepository orderRepository; // Injete o OrderRepository

    /**
     * Construtor que injeta o OrderRepository.
     * @param orderRepository O repositório para salvar os pedidos.
     */
    public MercadoPagoService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    /**
     * Cria a preferência de pagamento (Checkout Pro).
     */
    public PreferenceResponseDTO createPaymentPreference(PaymentRequestDTO dto) {
        // 1) Configura token
        logger.info("🔑 Using MP access token = {}", accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);

        try {
            // 2) Converte itens do DTO em PreferenceItemRequest
            List<PreferenceItemRequest> items = dto.getItems().stream()
                    .map(i -> PreferenceItemRequest.builder()
                            .id(i.getProductId())
                            .title(i.getProductName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .currencyId("BRL")
                            .build()
                    ).collect(Collectors.toList());

            // 3) Monta o bloco back_urls (obrigatório quando usar autoReturn)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:3000/checkout/success") // Ajuste para sua URL de frontend
                    .pending("http://localhost:3000/checkout/pending") // Ajuste para sua URL de frontend
                    .failure("http://localhost:3000/checkout/failure") // Ajuste para sua URL de frontend
                    .build();

            // 4) Constrói a PreferenceRequest
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(UUID.randomUUID().toString()) // Referência externa para seu pedido
                    .payer(PreferencePayerRequest.builder()
                            .email(dto.getPayerEmail())
                            .build())
                    .backUrls(backUrls)                  // ← Não remova!
                    .notificationUrl(
                            "http://localhost:9090/api/payments/webhook/mercadopago" // URL do webhook do seu backend
                    )
                    .build();

            // 5) Envia para o Mercado Pago
            Preference p = new PreferenceClient().create(request);

            if (p != null && p.getInitPoint() != null) {
                logger.info("Preferência de pagamento criada com sucesso. ID: {}. URL de Checkout: {}", p.getId(), p.getInitPoint());

                // NOVO: Salvar o pedido no MongoDB
                Order newOrder = new Order();
                newOrder.setId(new ObjectId().toHexString()); // Gera um ID para o seu pedido interno
                newOrder.setUserId(dto.getUserId());
                newOrder.setTotalAmount(dto.getTotalAmount());
                newOrder.setPaymentId(p.getId()); // Salva o ID da preferência do Mercado Pago
                newOrder.setPaymentStatus("PENDING_MP"); // Status inicial do pagamento no seu sistema
                newOrder.setStatus(OrderStatus.PENDING); // Status inicial do pedido no seu sistema

                // Mapear OrderItemDTO para OrderItem (seu modelo interno)
                newOrder.setItems(dto.getItems().stream()
                        .map(itemDto -> new br.com.ecommerce.meninadourada.model.OrderItem(
                                itemDto.getProductId(),
                                itemDto.getProductName(),
                                itemDto.getVariationId(),
                                itemDto.getQuantity(),
                                itemDto.getUnitPrice()
                        ))
                        .collect(Collectors.toList()));

                orderRepository.save(newOrder); // Salva o pedido no MongoDB
                logger.info("Pedido salvo no MongoDB com ID de preferência: {}", p.getId());

                // 6) Retorna ao controller
                return new PreferenceResponseDTO(p.getId(), p.getInitPoint());
            } else {
                logger.error("Falha ao criar preferência de pagamento. Resposta nula ou sem init_point.");
                throw new RuntimeException("Falha ao criar preferência de pagamento.");
            }

        } catch (MPApiException e) {
            int status = e.getStatusCode();
            String body  = e.getApiResponse().getContent();
            logger.error("🔴 MP API error. HTTP {} – {}", status, body);
            throw new RuntimeException("MP API error: HTTP " + status + " – " + body, e);

        } catch (MPException e) {
            logger.error("🔴 MP SDK error: {}", e.getMessage(), e);
            throw new RuntimeException("MP SDK error: " + e.getMessage(), e);

        } catch (Exception e) {
            logger.error("Erro inesperado ao criar preferência de pagamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado ao criar preferência de pagamento: " + e.getMessage(), e);
        }
    }

    /**
     * Lida com notificações do webhook do Mercado Pago.
     */
    public void handleWebhookNotification(String id, String topic) {
        MercadoPagoConfig.setAccessToken(accessToken);
        logger.info("🛈 Processing MP webhook. id={}, topic={}", id, topic);
        // aqui você pode buscar detalhes e atualizar seu pedido
    }
}
