package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferValidationPayload;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class AccountsValidator {
  private static final String NO_ACCOUNT_WITH_ID_FOUND = "No account with id %s found";

  // create an abstraction upon validation, e.g. ErrorsHolder which could be used across app for
  // business validation,
  // having valid toString method etc.

  // move validations to separate classes for the sake of better testing(assuming more complex logic
  // in real life as well)

  public Collection<String> validateTransfer(TransferValidationPayload validationPayload) {
    List<String> errors = new ArrayList<>();

    Account accountFrom = validationPayload.getAccountFrom();
    String accountFromId = validationPayload.getAccountFromId();
    Account accountTo = validationPayload.getAccountTo();
    String accountToId = validationPayload.getAccountToId();
    BigDecimal amount = validationPayload.getAmount();

    if (accountFrom == null) {
      errors.add(String.format(NO_ACCOUNT_WITH_ID_FOUND, accountFromId));
    }
    if (accountTo == null) {
      errors.add(String.format(NO_ACCOUNT_WITH_ID_FOUND, accountToId));
    }

    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      errors.add("Amount to transfer must be positive");
    }

    if (ObjectUtils.nullSafeEquals(accountTo, accountFrom)) {
      errors.add("Self transfer is not allowed");
    }

    if (accountFrom != null && !isWithdrawalPossible(accountFrom, amount)) {
      errors.add("Insufficient balance to perform transfer");
    }

    return errors;
  }

  private boolean isWithdrawalPossible(Account account, BigDecimal amountToWithdraw) {
    BigDecimal currentBalance = account.getBalance();
    return currentBalance.compareTo(amountToWithdraw) > -1;
  }
}
