package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferValidationPayload;
import com.db.awmd.challenge.service.AccountsValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collection;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsValidatorTest {

  @Autowired private AccountsValidator validator;

  @Test
  public void validates_account_to_not_empty() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);
    accountFrom.setBalance(amount);

    String uniqueIdTo = "IdTo-" + System.currentTimeMillis();

    TransferValidationPayload validationPayload =
        TransferValidationPayload.builder()
            .accountFrom(accountFrom)
            .accountFromId(uniqueIdFrom)
            .accountToId(uniqueIdTo)
            .amount(amount)
            .build();

    Collection<String> errors = validator.validateTransfer(validationPayload);

    assertThat(errors).containsExactly("No account with id " + uniqueIdTo + " found");
  }

  @Test
  public void validates_account_from_not_empty() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);
    accountFrom.setBalance(amount);

    String uniqueIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(uniqueIdTo);

    TransferValidationPayload validationPayload =
        TransferValidationPayload.builder()
            .accountFromId(uniqueIdFrom)
            .accountToId(uniqueIdTo)
            .accountTo(accountTo)
            .amount(amount)
            .build();

    Collection<String> errors = validator.validateTransfer(validationPayload);

    assertThat(errors).containsExactly("No account with id " + uniqueIdFrom + " found");
  }

  @Test
  public void validates_no_self_transfer() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);
    accountFrom.setBalance(amount);

    TransferValidationPayload validationPayload =
        TransferValidationPayload.builder()
            .accountFrom(accountFrom)
            .accountFromId(uniqueIdFrom)
            .accountTo(accountFrom)
            .accountToId(uniqueIdFrom)
            .amount(amount)
            .build();

    Collection<String> errors = validator.validateTransfer(validationPayload);
    assertThat(errors).containsExactly("Self transfer is not allowed");
  }

  @Test
  public void validates_enough_balance() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);

    String uniqueIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(uniqueIdTo);

    TransferValidationPayload validationPayload =
        TransferValidationPayload.builder()
            .accountFromId(uniqueIdFrom)
            .accountFrom(accountFrom)
            .accountToId(uniqueIdTo)
            .accountTo(accountTo)
            .amount(amount)
            .build();

    Collection<String> errors = validator.validateTransfer(validationPayload);

    assertThat(errors).containsExactly("Insufficient balance to perform transfer");
  }

  @Test
  public void validates_returns_empty_collection_for_valid_transfer() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);
    accountFrom.setBalance(BigDecimal.TEN);

    String uniqueIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(uniqueIdTo);

    TransferValidationPayload validationPayload =
        TransferValidationPayload.builder()
            .accountFromId(uniqueIdFrom)
            .accountFrom(accountFrom)
            .accountToId(uniqueIdTo)
            .accountTo(accountTo)
            .amount(amount)
            .build();

    Collection<String> errors = validator.validateTransfer(validationPayload);

    assertThat(errors).isEmpty();
  }
}
