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
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

// NOVO: Importações para dados de endereço e telefone do Mercado Pago (pacote common)
import com.mercadopago.client.common.AddressRequest; // Adicionado
import com.mercadopago.client.common.PhoneRequest; // Adicionado
import com.mercadopago.client.common.IdentificationRequest; // Adicionado
import com.mercadopago.client.preference.PreferenceReceiverAddressRequest;
import com.mercadopago.client.preference.PreferenceShipmentsRequest;



@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final OrderRepository orderRepository;
    private final PreferenceClient preferenceClient;
    private final PaymentClient paymentClient;
    private final MerchantOrderClient merchantOrderClient;
    private final EmailService emailService; // NOVO: Injete o EmailService

    @Autowired
    public MercadoPagoService(OrderRepository orderRepository, EmailService emailService) { // NOVO: Adicione EmailService ao construtor
        this.orderRepository = orderRepository;
        this.preferenceClient = new PreferenceClient();
        this.paymentClient = new PaymentClient();
        this.merchantOrderClient = new MerchantOrderClient();
        this.emailService = emailService; // Atribua o EmailService
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
                            .unitPrice(i.getUnitPrice())
                            .currencyId("BRL")
                            .build()
                    ).collect(Collectors.toList());

            // Gerar a externalReference ANTES de criar a preferência para poder salvá-la no Order
            String orderExternalReference = UUID.randomUUID().toString();

            // Construir o PayerRequest com dados completos do cliente
            PreferencePayerRequest mpPayerRequest = PreferencePayerRequest.builder()
                    .name(dto.getCustomerName())
                    .email(dto.getPayerEmail())
                    .phone(PhoneRequest.builder()
                            .areaCode(dto.getCustomerPhone().substring(0, 2)) // Assumindo DDD nos 2 primeiros dígitos
                            .number(dto.getCustomerPhone().substring(2)) // Restante do número
                            .build())
                    .identification(IdentificationRequest.builder()
                            .type("CPF") // Tipo de documento
                            .number(dto.getCustomerCpf()) // Número do CPF
                            .build())
                    .address(AddressRequest.builder() // Endereço do pagador
                            .zipCode(dto.getShippingAddress().getZipCode())
                            .streetName(dto.getShippingAddress().getStreetName())
                            .streetNumber(dto.getShippingAddress().getStreetNumber())
                            .city(dto.getShippingAddress().getCityName()) // Campo 'city' no MP
                            .state(dto.getShippingAddress().getStateName()) // Campo 'state' no MP
                            .build())
                    .build();

            // Construir o ShipmentsRequest com dados de entrega
            PreferenceShipmentsRequest mpShipmentsRequest = PreferenceShipmentsRequest.builder()
                    .receiverAddress(PreferenceReceiverAddressRequest.builder()
                            .zipCode(dto.getShippingAddress().getZipCode())
                            .streetName(dto.getShippingAddress().getStreetName())
                            .streetNumber(dto.getShippingAddress().getStreetNumber())
                            .cityName(dto.getShippingAddress().getCityName())
                            .stateName(dto.getShippingAddress().getStateName())
                            .countryName(dto.getShippingAddress().getCountryName())
                            .build())
                    .build();


            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://meninadourada.shop/checkout/success")
                    .pending("https://meninadourada.shop/checkout/failure")
                    .failure("https://meninadourada.shop/checkout/rejected")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(orderExternalReference)
                    .payer(mpPayerRequest)
                    .shipments(mpShipmentsRequest) // Adicionar informações de envio
                    .backUrls(backUrls)
                    .autoReturn("all")
                    .notificationUrl("https://meninadourada.shop/api/payments/webhook/mercadopago") // Sua URL de webhook
                    .build();

            Preference p = new PreferenceClient().create(request);

            if (p != null && p.getInitPoint() != null) {
                logger.info("Payment preference created successfully. ID: {}. Checkout URL: {}", p.getId(), p.getInitPoint());

                // Salvar o pedido no MongoDB com o ID da preferência E a externalReference
                Order newOrder = new Order();
                newOrder.setId(new ObjectId().toHexString());
                newOrder.setUserId(dto.getUserId());
                newOrder.setTotalAmount(dto.getTotalAmount());
                newOrder.setPaymentId(p.getId());
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
                newOrder.setExternalReference(orderExternalReference);
                // Popular os dados do cliente e endereço no seu objeto Order
                newOrder.setCustomerName(dto.getCustomerName());
                newOrder.setCustomerEmail(dto.getPayerEmail());
                newOrder.setCustomerPhone(dto.getCustomerPhone());
                newOrder.setCustomerCpf(dto.getCustomerCpf());
                newOrder.setShippingAddress(dto.getShippingAddress());

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
     * Lida com notificações do webhook do Mercado Pago.
     * Esta função é crucial para atualizar o status do pedido no seu sistema.
     *
     * @param id Notification ID or payment ID.
     * @param topic Notification topic (e.g., "payment", "merchant_order").
     */
    @Transactional // Garante que a atualização do pedido seja atômica
    public void handleWebhookNotification(String id, String topic) {
        logger.info("🛈 Processing MP webhook. id={}, topic={}", id, topic);
        configureMercadoPagoSdk();

        try {
            if ("payment".equals(topic)) {
                Payment payment = paymentClient.get(Long.valueOf(id));
                logger.info("Detalhes do Payment (ID: {}): Status: {}, External Reference: {}",
                        payment.getId(), payment.getStatus(), payment.getExternalReference());

                Order order = orderRepository.findByExternalReference(payment.getExternalReference())
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para External Reference: " + payment.getExternalReference()));

                order.setPaymentStatus(payment.getStatus());
                order.setStatus(mapMercadoPagoStatusToOrderStatus(payment.getStatus()));

                orderRepository.save(order);
                logger.info("Pedido {} atualizado via webhook. Novo status: {}. Status MP: {}",
                        order.getId(), order.getStatus(), payment.getStatus());

                // NOVO: Enviar e-mails após a aprovação do pagamento
                if ("approved".equals(payment.getStatus())) {
                    emailService.sendOrderConfirmationEmailToCustomer(order); // Envia para o cliente
                    emailService.sendNewSaleNotificationToStore(order); // Envia para a loja
                }

            } else if ("merchant_order".equals(topic)) {
                MerchantOrder merchantOrder = merchantOrderClient.get(Long.valueOf(id));
                logger.info("Detalhes da Merchant Order (ID: {}): Status: {}, External Reference: {}",
                        merchantOrder.getId(), merchantOrder.getOrderStatus(), merchantOrder.getExternalReference());

                Order order = orderRepository.findByExternalReference(merchantOrder.getExternalReference())
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para Merchant Order External Reference: " + merchantOrder.getExternalReference()));

                order.setPaymentStatus(merchantOrder.getOrderStatus());
                order.setStatus(mapMercadoPagoStatusToOrderStatus(merchantOrder.getOrderStatus()));
                orderRepository.save(order);
                logger.info("Pedido {} atualizado via webhook (Merchant Order). Novo status: {}. Status MO: {}",
                        order.getId(), order.getStatus(), merchantOrder.getOrderStatus());

                // NOVO: Enviar e-mails após a aprovação da Merchant Order (se o status for final de aprovação)
                if ("closed".equals(merchantOrder.getOrderStatus()) || "paid".equals(merchantOrder.getOrderStatus())) { // 'closed' ou 'paid' indicam finalização
                    emailService.sendOrderConfirmationEmailToCustomer(order);
                    emailService.sendNewSaleNotificationToStore(order);
                }

            } else {
                logger.warn("Webhook com tópico desconhecido ou não processado: {}", topic);
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Erro no webhook: {}", e.getMessage());
        } catch (MPApiException e) {
            logger.error("🔴 MP API error no webhook. Status: {}, Resposta: {}", e.getStatusCode(), e.getApiResponse().getContent());
        } catch (MPException e) {
            logger.error("🔴 MP SDK error no webhook: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar webhook: {}", e.getMessage(), e);
        }
    }

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
            default:
                return OrderStatus.PENDING;
        }
    }
}
