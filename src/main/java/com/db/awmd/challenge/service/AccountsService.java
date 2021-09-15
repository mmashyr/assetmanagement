package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferValidationPayload;
import com.db.awmd.challenge.exception.TransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;

@Service
public class AccountsService {

  @Getter private final AccountsRepository accountsRepository;

  @Getter private final AccountsValidator accountsValidator;

  @Getter private final NotificationService notificationService;

  @Autowired
  public AccountsService(
      AccountsRepository accountsRepository,
      AccountsValidator accountsValidator,
      NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.accountsValidator = accountsValidator;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void performTransfer(String accountIdFrom, String accountIdTo, BigDecimal amount) {
    Account accountFrom = accountsRepository.getAccount(accountIdFrom);
    Account accountTo = accountsRepository.getAccount(accountIdTo);

    Account first = accountFrom;
    Account second = accountTo;
    if (accountFrom.getAccountId().compareTo(accountTo.getAccountId()) < 0) {
      first = accountTo;
      second = accountFrom;
    }
    synchronized (first) {
      synchronized (second) {
        TransferValidationPayload validationPayload =
            TransferValidationPayload.builder()
                .accountFrom(accountFrom)
                .accountFromId(accountIdFrom)
                .accountTo(accountTo)
                .accountToId(accountIdTo)
                .amount(amount)
                .build();
        Collection<String> errors = accountsValidator.validateTransfer(validationPayload);
        if (!CollectionUtils.isEmpty(errors)) {
          throw new TransferException(String.join(", ", errors));
        }

        accountsRepository.transfer(
            accountFrom,
            accountFrom.getBalance().subtract(amount),
            accountTo,
            accountTo.getBalance().add(amount));

        notifyAboutTransfer(amount, accountFrom, accountTo);
      }
    }
  }

  private void notifyAboutTransfer(BigDecimal amount, Account accountFrom, Account accountTo) {
    notificationService.notifyAboutTransfer(
        accountTo, String.format("Transfer from account %s, quantity: %s", accountFrom, amount));
    notificationService.notifyAboutTransfer(
        accountFrom, String.format("Transfer to account %s, quantity: %s", accountTo, amount));
  }
}
