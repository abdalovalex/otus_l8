package org.example.orderservice.repository;

import java.util.Optional;

import org.example.orderservice.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByHash(String hash);
}
