package org.example.billingservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.billingservice.config.BillingConfiguration;
import org.example.billingservice.dto.CreateAccountDTO;
import org.example.billingservice.dto.IncomeMoneyDTO;
import org.example.billingservice.entity.Account;
import org.example.billingservice.entity.Operation;
import org.example.billingservice.entity.Type;
import org.example.billingservice.event.BankBalanceEvent;
import org.example.billingservice.event.BankTransactionEvent;
import org.example.billingservice.exception.AccountException;
import org.example.billingservice.mapper.AccountMapper;
import org.example.billingservice.mapper.OperationMapper;
import org.example.billingservice.repository.AccountRepository;
import org.example.billingservice.repository.BankBalanceRepository;
import org.example.billingservice.repository.OperationRepository;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    private final AccountMapper accountMapper;
    private final OperationMapper operationMapper;
    private final StreamBridge streamBridge;
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final BankBalanceRepository bankBalanceRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public void create(CreateAccountDTO createAccountDTO) throws AccountException {
        if (accountRepository.existsByAccount(createAccountDTO.getAccount())) {
            throw new AccountException("Счет уже создан");
        }
        Account account = accountMapper.map(createAccountDTO);
        accountRepository.save(account);
    }

    public void income(IncomeMoneyDTO incomeMoneyDTO) throws AccountException {
        Account account = accountRepository.findByAccount(incomeMoneyDTO.getAccount())
                .orElseThrow(() -> new AccountException("Счет не найден"));

        if (operationRepository.existsByHash(incomeMoneyDTO.getHash())) {
            throw new AccountException("Операция уже была выполнена");
        }

        transactionTemplate.execute(status -> {
            Operation operation = operationMapper.map(incomeMoneyDTO);
            operation.setAccount(account);
            operation.setType(Type.INFLOW);
            operationRepository.save(operation);

            return status;
        });

        BankTransactionEvent operationEvent = BankTransactionEvent.builder()
                .account(account.getAccount())
                .amount(incomeMoneyDTO.getAmount())
                .hash(incomeMoneyDTO.getHash())
                .build();
        streamBridge.send(BillingConfiguration.BANK_TRANSACTION,
                          MessageBuilder
                                  .withPayload(operationEvent)
                                  .setHeader(KafkaHeaders.KEY, incomeMoneyDTO.getAccount())
                                  .build());
    }

    public BankBalanceEvent get(String accountId) throws Exception {
        return bankBalanceRepository.find(accountId)
                .orElseThrow(() -> new AccountException("Счет не найден"));
    }
}
