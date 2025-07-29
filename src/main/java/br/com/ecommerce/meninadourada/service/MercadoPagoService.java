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
// Importações para buscar detalhes de pagamento/ordem via API
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.resources.merchantorder.MerchantOrder;

import br.com.ecommerce.meninadourada.dto.OrderItemDTO;
import br.com.ecommerce.meninadourada.dto.PaymentRequestDTO;
import br.com.ecommerce.meninadourada.dto.PreferenceResponseDTO;
import br.com.ecommerce.meninadourada.model.Order; // Sua entidade Order
import br.com.ecommerce.meninadourada.model.OrderStatus;
import br.com.ecommerce.meninadourada.repository.OrderRepository;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException; // Para quando o pedido não é encontrado

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para transações no MongoDB

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final OrderRepository orderRepository;
    private final PreferenceClient preferenceClient;
    private final PaymentClient paymentClient; // Cliente para buscar detalhes de Payment
    private final MerchantOrderClient merchantOrderClient; // Cliente para buscar detalhes de MerchantOrder

    @Autowired
    public MercadoPagoService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.preferenceClient = new PreferenceClient();
        this.paymentClient = new PaymentClient(); // Instancia PaymentClient
        this.merchantOrderClient = new MerchantOrderClient(); // Instancia MerchantOrderClient
    }

    private void configureMercadoPagoSdk() {
        if (MercadoPagoConfig.getAccessToken() == null || !MercadoPagoConfig.getAccessToken().equals(accessToken)) {
            MercadoPagoConfig.setAccessToken(accessToken);
            logger.info("Mercado Pago Access Token configurado no SDK.");
        }
    }

    public PreferenceResponseDTO createPaymentPreference(PaymentRequestDTO dto) {
        logger.info("Starting creation of payment preference for user: {}", dto.getUserId());
        configureMercadoPagoSdk();

        try {
            List<PreferenceItemRequest> items = dto.getItems().stream()
                    .map(i -> PreferenceItemRequest.builder()
                            .id(i.getProductId())
                            .title(i.getProductName() + " - " + i.getVariationId())
                            .quantity(Math.toIntExact(i.getQuantity()))
                            // CORRIGIDO: Removida a conversão para Long.
                            .unitPrice(i.getUnitPrice())
                            .currencyId("BRL")
                            .build()
                    ).collect(Collectors.toList());

            // Gerar a externalReference ANTES de criar a preferência para poder salvá-la no Order
            String orderExternalReference = UUID.randomUUID().toString();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://meninadourada.shop/checkout/success")
                    .pending("https://meninadourada.shop/checkout/failure")
                    .failure("https://meninadourada.shop/checkout/pending")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(orderExternalReference) // Usar a externalReference gerada
                    .payer(com.mercadopago.client.preference.PreferencePayerRequest.builder()
                            .email(dto.getPayerEmail())
                            .build())
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .notificationUrl("http://18.228.9.73:9090/api/payments/webhook/mercadopago") // Sua URL de webhook
                    .build();

            Preference p = new PreferenceClient().create(request);

            if (p != null && p.getInitPoint() != null) {
                logger.info("Payment preference created successfully. ID: {}. Checkout URL: {}", p.getId(), p.getInitPoint());

                // Salvar o pedido no MongoDB com o ID da preferência E a externalReference
                Order newOrder = new Order();
                newOrder.setId(new ObjectId().toHexString());
                newOrder.setUserId(dto.getUserId());
                newOrder.setTotalAmount(dto.getTotalAmount());
                newOrder.setPaymentId(p.getId()); // ID da preferência do Mercado Pago
                newOrder.setPaymentStatus("PENDING_CHECKOUT_MP");
                newOrder.setStatus(OrderStatus.PENDING);
                newOrder.setItems(dto.getItems().stream()
                        .map(itemDto -> new br.com.ecommerce.meninadourada.model.OrderItem(
                                itemDto.getProductId(),
                                itemDto.getProductName(),
                                itemDto.getVariationId(),
                                itemDto.getQuantity(),
                                itemDto.getUnitPrice()
                        )).collect(Collectors.toList()));
                // NOVO: Salvar a externalReference no seu objeto Order para fácil busca no webhook
                newOrder.setExternalReference(orderExternalReference); // Adicionar este setter ao seu modelo Order
                orderRepository.save(newOrder);
                logger.info("Order saved in MongoDB with preference ID: {} and External Reference: {}", p.getId(), orderExternalReference);

                return new PreferenceResponseDTO(p.getId(), p.getInitPoint());
            } else {
                logger.error("Failed to create payment preference. Null response or no init_point.");
                throw new RuntimeException("Failed to create payment preference.");
            }

        } catch (MPApiException e) {
            int status = e.getStatusCode();
            String body = e.getApiResponse().getContent();
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
     * Maps Mercado Pago Order status to your OrderStatus enum.
     * @param mpStatus Mercado Pago Order status.
     * @return Your corresponding OrderStatus enum.
     */
    private OrderStatus mapMercadoPagoStatusToOrderStatus(String mpStatus) {
        switch (mpStatus) {
            case "approved":
                return OrderStatus.PAID;
            case "pending":
                return OrderStatus.PENDING;
            case "rejected":
                return OrderStatus.REJECTED;
            case "cancelled":
                return OrderStatus.CANCELLED;
            // Adicione outros mapeamentos conforme a documentação do Mercado Pago
            default:
                return OrderStatus.PENDING; // Default or unknown status
        }
    }

    /**
     * Lida com notificações do webhook do Mercado Pago.
     * Esta função é crucial para atualizar o status do pedido no seu sistema.
     *
     * @param id ID da notificação ou ID do pagamento/merchant_order.
     * @param topic Tópico da notificação (ex: "payment", "merchant_order").
     */
    @Transactional // Garante que a atualização do pedido seja atômica
    public void handleWebhookNotification(String id, String topic) {
        logger.info("🛈 Processing MP webhook. id={}, topic={}", id, topic);
        configureMercadoPagoSdk(); // Garante que o token está configurado para as chamadas da API do MP

        try {
            if ("payment".equals(topic)) {
                // Notificação de um pagamento específico
                // CORRIGIDO: Converter String id para Long
                Payment payment = paymentClient.get(Long.valueOf(id)); // Busca os detalhes do pagamento
                logger.info("Detalhes do Payment (ID: {}): Status: {}, External Reference: {}",
                        payment.getId(), payment.getStatus(), payment.getExternalReference());

                // Buscar o pedido no seu DB pela externalReference do Payment
                // A externalReference do Payment DEVE ser o mesmo UUID que você gerou para o Order.externalReference
                Order order = orderRepository.findByExternalReference(payment.getExternalReference())
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para External Reference: " + payment.getExternalReference()));

                // Atualizar o status do pedido no seu sistema
                order.setPaymentStatus(payment.getStatus()); // Status do pagamento do Mercado Pago
                order.setStatus(mapMercadoPagoStatusToOrderStatus(payment.getStatus())); // Mapeia para seu enum

                orderRepository.save(order);
                logger.info("Pedido {} atualizado via webhook. Novo status: {}. Status MP: {}",
                        order.getId(), order.getStatus(), payment.getStatus());

                // Aqui você também daria baixa no estoque, enviaria e-mails de confirmação, etc.

            } else if ("merchant_order".equals(topic)) {
                // Notificação de uma ordem de compra (agrupamento de pagamentos)
                // CORRIGIDO: Converter String id para Long
                MerchantOrder merchantOrder = merchantOrderClient.get(Long.valueOf(id)); // Busca os detalhes da Merchant Order
                logger.info("Detalhes da Merchant Order (ID: {}): Status: {}, External Reference: {}",
                        merchantOrder.getId(), merchantOrder.getOrderStatus(), merchantOrder.getExternalReference());

                // Buscar o pedido no seu DB pela externalReference da MerchantOrder
                // A externalReference da MerchantOrder DEVE ser o mesmo UUID que você gerou para o Order.externalReference
                Order order = orderRepository.findByExternalReference(merchantOrder.getExternalReference())
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para Merchant Order External Reference: " + merchantOrder.getExternalReference()));

                // Atualizar status baseado na Merchant Order
                order.setPaymentStatus(merchantOrder.getOrderStatus());
                order.setStatus(mapMercadoPagoStatusToOrderStatus(merchantOrder.getOrderStatus()));
                orderRepository.save(order);
                logger.info("Pedido {} atualizado via webhook (Merchant Order). Novo status: {}. Status MO: {}",
                        order.getId(), order.getStatus(), merchantOrder.getOrderStatus());

            } else {
                logger.warn("Webhook com tópico desconhecido ou não processado: {}", topic);
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Erro no webhook: {}", e.getMessage());
            // Não relance a exceção para o Mercado Pago, apenas logue.
        } catch (MPApiException e) {
            logger.error("🔴 MP API error no webhook. Status: {}, Resposta: {}", e.getStatusCode(), e.getApiResponse().getContent());
        } catch (MPException e) {
            logger.error("🔴 MP SDK error no webhook: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar webhook: {}", e.getMessage(), e);
        }
    }
}
