package org.example.billingservice.repository;

import org.example.billingservice.entity.Operation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Integer> {
    boolean existsByHash(String hash);
}
