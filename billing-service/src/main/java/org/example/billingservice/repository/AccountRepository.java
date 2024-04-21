package org.example.billingservice.repository;

import java.util.Optional;

import org.example.billingservice.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByAccount(String account);

    boolean existsByAccount(String account);
}
