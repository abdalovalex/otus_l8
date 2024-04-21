package org.example.billingservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.billingservice.dto.CreateAccountDTO;
import org.example.billingservice.dto.IncomeMoneyDTO;
import org.example.billingservice.event.BankBalanceEvent;
import org.example.billingservice.exception.AccountException;
import org.example.billingservice.service.BillingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final BillingService service;

    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody CreateAccountDTO createAccountDTO) {
        try {
            service.create(createAccountDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (AccountException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PatchMapping("/put")
    public ResponseEntity<Void> put(@RequestBody IncomeMoneyDTO incomeMoneyDTO) {
        try {
            service.income(incomeMoneyDTO);
            return ResponseEntity.ok().build();
        } catch (AccountException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankBalanceEvent> get(@PathVariable String accountId) {
        try {
            return ResponseEntity.ok(service.get(accountId));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
