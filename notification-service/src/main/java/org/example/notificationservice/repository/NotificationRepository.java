package org.example.notificationservice.repository;

import java.util.Optional;

import org.example.notificationservice.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Optional<Notification> findByOrderId(Integer orderId);
}
