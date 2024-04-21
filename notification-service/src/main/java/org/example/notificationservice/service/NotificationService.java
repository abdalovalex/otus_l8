package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.NotificationDTO;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.event.OrderTransactionApprovedEvent;
import org.example.notificationservice.event.OrderTransactionRejectedEvent;
import org.example.notificationservice.exception.NotificationException;
import org.example.notificationservice.mapper.NotificationMapper;
import org.example.notificationservice.repository.NotificationRepository;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;

    public void sendPositive(OrderTransactionApprovedEvent orderTransactionApprovedEvent) {
        Notification notification = new Notification();
        notification.setOrderId(orderTransactionApprovedEvent.getOrderId());
        notification.setText("Заказ %d оплачен".formatted(orderTransactionApprovedEvent.getOrderId()));
        notificationRepository.save(notification);
    }

    public void sendNegative(OrderTransactionRejectedEvent orderTransactionRejectedEvent) {
        Notification notification = new Notification();
        notification.setOrderId(orderTransactionRejectedEvent.getOrderId());
        notification.setText("Для заказа %d необходимо пополнить счет".formatted(orderTransactionRejectedEvent.getOrderId()));
        notificationRepository.save(notification);
    }

    public NotificationDTO get(Integer orderId) throws NotificationException {
        Notification notification = notificationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotificationException("Письмо не найдено"));
        return notificationMapper.map(notification);
    }
}
