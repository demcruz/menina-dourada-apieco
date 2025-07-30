package br.com.ecommerce.meninadourada.service;

import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import br.com.ecommerce.meninadourada.model.*;
import br.com.ecommerce.meninadourada.repository.OrderRepository;
import br.com.ecommerce.meninadourada.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bson.types.ObjectId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    private final OrderRepository orderRepository;
    private final ProdutoRepository produtoRepository; // To check stock

    @Autowired
    public OrderService(OrderRepository orderRepository, ProdutoRepository produtoRepository) {
        this.orderRepository = orderRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Creates a new order in the system.
     * This function is called BEFORE interacting with the payment gateway.
     * It should check stock availability.
     *
     * @param order The Order object to be created.
     * @return The created Order.
     * @throws IllegalArgumentException If stock is insufficient.
     */
    @Transactional
    public Order createOrder(Order order) {
        logger.info("Starting order creation for user: {}", order.getUserId());

        // 1. Generate ID for the order
        order.setId(new ObjectId().toHexString());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING); // Initial status

        // 2. Check stock and calculate total (simplified)
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            Produto produto = produtoRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            VariacaoProduto variacao = produto.getVariacoes().stream()
                    .filter(v -> v.getId().equals(item.getVariationId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product variation not found with ID: " + item.getVariationId()));

            if (variacao.getEstoque() < item.getQuantity()) {
                logger.warn("Insufficient stock for variation {} of product {}. Available: {}, Requested: {}",
                        item.getVariationId(), item.getProductName(), variacao.getEstoque(), item.getQuantity());
                throw new IllegalArgumentException("Insufficient stock for product: " + item.getProductName() + " - " + variacao.getCor() + " " + variacao.getTamanho());
            }
            // Update stock (in a real scenario, this would be done after payment confirmation)
            // variacao.setEstoque(variacao.getEstoque() - item.getQuantity());
            // produtoRepository.save(produto); // Save stock update

            calculatedTotal = calculatedTotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(calculatedTotal);

        // 3. Save the order in the database (PENDING status)
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} created successfully for user {}. Status: {}", savedOrder.getId(), savedOrder.getUserId(), savedOrder.getStatus());
        return savedOrder;
    }

    /**
     * Retrieves an order by its ID.
     * @param id The order ID.
     * @return The retrieved Order.
     * @throws ResourceNotFoundException If the order is not found.
     */
    public Order getOrderById(String id) {
        logger.info("Retrieving order with ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Order with ID {} not found.", id);
                    return new ResourceNotFoundException("Order not found with ID: " + id);
                });
    }

    /**
     * Updates the status of an order.
     * @param id The order ID.
     * @param newStatus The new order status.
     * @return The updated Order.
     * @throws ResourceNotFoundException If the order is not found.
     */
    @Transactional
    public Order updateOrderStatus(String id, OrderStatus newStatus) {
        logger.info("Updating status of order {} to {}.", id, newStatus);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order {} status updated successfully to {}.", updatedOrder.getId(), updatedOrder.getStatus());
        return updatedOrder;
    }

    /**
     * Lists all orders.
     * @return A list of all orders.
     */
    public List<Order> getAllOrders() {
        logger.info("Listing all orders.");
        return orderRepository.findAll();
    }



    public boolean updateOrderAfterPayment(String preferenceId, String paymentId, String paymentStatus) {
        Optional<Order> optionalOrder = orderRepository.findByPaymentId(preferenceId); // Lembrando: você salvou o preferenceId como paymentId inicialmente

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setPaymentId(paymentId);
            order.setPaymentStatus(paymentStatus);
            orderRepository.save(order);
            log.info("🟢 Pedido atualizado com paymentId {} e status {}", paymentId, paymentStatus);
            return true;
        } else {
            log.warn("⚠️ Nenhum pedido encontrado com preferenceId: {}", preferenceId);
            return false;
        }
    }


}