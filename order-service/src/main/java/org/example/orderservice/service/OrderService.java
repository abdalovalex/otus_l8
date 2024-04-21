package org.example.orderservice.service;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.OrderDTO;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderStatus;
import org.example.orderservice.event.OrderEvent;
import org.example.orderservice.exception.OrderException;
import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.repository.OrderRepository;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final StreamBridge streamBridge;
    private final OrderMapper orderMapper;
    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;

    public Integer create(OrderDTO orderDTO) throws OrderException {
        final Order order = orderMapper.map(orderDTO);

        if (orderRepository.existsByHash(order.getHash())) {
            throw new OrderException("Заказ уже был создан");
        }

        Integer orderId = transactionTemplate.execute(status -> {
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
            return order.getId();
        });

        if (orderId == null) {
            throw new OrderException("Не удалось создать заказ");
        }

        BigDecimal amount = orderDTO.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            amount = amount.multiply(BigDecimal.valueOf(-1));
        }

        OrderEvent operationEvent = OrderEvent.builder()
                .orderId(orderId)
                .account(orderDTO.getAccount())
                .amount(amount)
                .hash(orderDTO.getHash())
                .build();
        streamBridge.send("order",
                          MessageBuilder
                                  .withPayload(operationEvent)
                                  .setHeader(KafkaHeaders.KEY, operationEvent.getOrderId().toString())
                                  .build());
        return orderId;
    }
}
