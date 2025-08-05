package br.com.ecommerce.meninadourada.service;


import br.com.ecommerce.meninadourada.model.Order; // Importe sua entidade Order
import br.com.ecommerce.meninadourada.model.OrderItem; // Importe sua entidade OrderItem
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException; // Importe para tratamento de exceções de e-mail
import org.springframework.mail.SimpleMailMessage; // Importe para e-mails simples
import org.springframework.mail.javamail.JavaMailSender; // Importe para enviar e-mails
import org.springframework.stereotype.Service;

@Service
public class EmailService {


    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetenteEmail; // E-mail do remetente (configurado no application.properties)

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia um e-mail de confirmação de compra para o cliente.
     *
     * @param order O objeto Order completo com os detalhes do pedido.
     */
    public void sendOrderConfirmationEmailToCustomer(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetenteEmail);
        message.setTo(order.getCustomerEmail()); // E-mail do cliente
        message.setSubject("Parabéns pela sua compra na Menina Dourada! Pedido #" + order.getId());

        StringBuilder text = new StringBuilder();
        text.append("Olá ").append(order.getCustomerName()).append(",\n\n");
        text.append("Parabéns pela sua compra na Menina Dourada! Seu pedido foi aprovado e está sendo processado.\n\n");
        text.append("Detalhes do Pedido:\n");
        text.append("Número do Pedido: #").append(order.getId()).append("\n");
        text.append("Data do Pedido: ").append(order.getOrderDate()).append("\n");
        text.append("Valor Total: R$ ").append(order.getTotalAmount()).append("\n\n");

        text.append("Itens do Pedido:\n");
        for (OrderItem item : order.getItems()) {
            text.append("- ").append(item.getQuantity()).append("x ")
                    .append(item.getProductName()).append(" (Variação: ").append(item.getVariationId())
                    .append(") - R$ ").append(item.getUnitPrice()).append(" cada\n");
        }
        text.append("\n");

        text.append("Endereço de Entrega:\n");
        text.append(order.getCustomerName()).append("\n");
        text.append(order.getShippingAddress().getStreetName()).append(", ")
                .append(order.getShippingAddress().getStreetNumber());
        if (order.getShippingAddress().getComplement() != null && !order.getShippingAddress().getComplement().isEmpty()) {
            text.append(" - ").append(order.getShippingAddress().getComplement());
        }
        text.append("\n");
        text.append(order.getShippingAddress().getNeighborhood()).append(", ")
                .append(order.getShippingAddress().getCityName()).append(" - ")
                .append(order.getShippingAddress().getStateName()).append("\n");
        text.append("CEP: ").append(order.getShippingAddress().getZipCode()).append("\n");
        text.append("País: ").append(order.getShippingAddress().getCountryName()).append("\n\n");

        text.append("Agradecemos a sua preferência!\n");
        text.append("Equipe Menina Dourada.");

        message.setText(text.toString());

        try {
            mailSender.send(message);
            logger.info("E-mail de confirmação de pedido enviado para: {}", order.getCustomerEmail());
        } catch (MailException e) {
            logger.error("Falha ao enviar e-mail de confirmação de pedido para {}: {}", order.getCustomerEmail(), e.getMessage(), e);
            // Em produção, você pode querer re-tentar ou notificar um sistema de monitoramento
        }
    }

    /**
     * Envia um e-mail de notificação de nova venda para a loja (Menina Dourada).
     *
     * @param order O objeto Order completo com os detalhes do pedido.
     */
    public void sendNewSaleNotificationToStore(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetenteEmail);
        message.setTo(remetenteEmail); // Envia para o próprio e-mail da loja
        message.setSubject("NOVA VENDA APROVADA! Pedido #" + order.getId());

        StringBuilder text = new StringBuilder();
        text.append("Uma nova venda foi aprovada na loja Menina Dourada!\n\n");
        text.append("Detalhes do Pedido:\n");
        text.append("Número do Pedido: #").append(order.getId()).append("\n");
        text.append("Data do Pedido: ").append(order.getOrderDate()).append("\n");
        text.append("Valor Total: R$ ").append(order.getTotalAmount()).append("\n");
        text.append("Status de Pagamento: ").append(order.getPaymentStatus()).append("\n");
        text.append("Referência Externa (MP): ").append(order.getExternalReference()).append("\n\n");

        text.append("Cliente:\n");
        text.append("Nome: ").append(order.getCustomerName()).append("\n");
        text.append("Email: ").append(order.getCustomerEmail()).append("\n");
        text.append("Telefone: ").append(order.getCustomerPhone()).append("\n");
        text.append("CPF: ").append(order.getCustomerCpf()).append("\n\n");

        text.append("Itens da Venda:\n");
        for (OrderItem item : order.getItems()) {
            text.append("- ").append(item.getQuantity()).append("x ")
                    .append(item.getProductName() != null ? item.getProductName() : "Produto")
                    .append(item.getProductName()).append(" (Variação: ").append(item.getProductName())
                    .append(") - R$ ").append(item.getUnitPrice()).append(" cada\n");
        }
        text.append("\n");

        text.append("Endereço de Entrega:\n");
        text.append(order.getShippingAddress().getStreetName()).append(", ")
                .append(order.getShippingAddress().getStreetNumber());
        if (order.getShippingAddress().getComplement() != null && !order.getShippingAddress().getComplement().isEmpty()) {
            text.append(" - ").append(order.getShippingAddress().getComplement());
        }
        text.append("\n");
        text.append(order.getShippingAddress().getNeighborhood()).append(", ")
                .append(order.getShippingAddress().getCityName()).append(" - ")
                .append(order.getShippingAddress().getStateName()).append("\n");
        text.append("CEP: ").append(order.getShippingAddress().getZipCode()).append("\n");
        text.append("País: ").append(order.getShippingAddress().getCountryName()).append("\n\n");

        message.setText(text.toString());

        try {
            mailSender.send(message);
            logger.info("E-mail de notificação de nova venda enviado para a loja.");
        } catch (MailException e) {
            logger.error("Falha ao enviar e-mail de notificação de nova venda para a loja: {}", e.getMessage(), e);
        }
    }
}
