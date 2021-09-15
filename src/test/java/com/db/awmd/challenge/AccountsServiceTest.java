package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferValidationPayload;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.AccountsValidator;
import com.db.awmd.challenge.service.NotificationService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired private AccountsService accountsService;

  @Autowired private AccountsValidator accountsValidator;

  @Autowired private NotificationService notificationService;

  @Test
  public void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    ThrowingCallable callable = () -> this.accountsService.createAccount(account);
    assertThatThrownBy(callable).hasMessage("Account id " + uniqueId + " already exists!");
  }

  @Test
  public void performTransfer_throwsValidationErrorsIfOccursNoBalanceChanges() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);
    accountFrom.setBalance(amount);
    this.accountsService.createAccount(accountFrom);

    String uniqueIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(uniqueIdTo);
    this.accountsService.createAccount(accountTo);

    TransferValidationPayload validationPayload =
        TransferValidationPayload.builder()
            .accountFrom(accountFrom)
            .accountFromId(uniqueIdFrom)
            .accountTo(accountTo)
            .accountToId(uniqueIdTo)
            .amount(amount)
            .build();

    List<String> errors = new ArrayList<>();
    errors.add("error");
    given(accountsValidator.validateTransfer(eq(validationPayload))).willReturn(errors);

    ThrowingCallable callable =
        () -> accountsService.performTransfer(uniqueIdFrom, uniqueIdTo, amount);

    assertThatThrownBy(callable).hasMessage("error");
    assertThat(accountFrom.getBalance()).isEqualTo(BigDecimal.TEN);
    assertThat(accountTo.getBalance()).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  public void performTransfer_performsTransferAndSendsSuccessMessages() {
    BigDecimal amount = BigDecimal.TEN;
    String uniqueIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(uniqueIdFrom);
    accountFrom.setBalance(amount);
    this.accountsService.createAccount(accountFrom);

    String uniqueIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(uniqueIdTo);
    this.accountsService.createAccount(accountTo);

    accountsService.performTransfer(uniqueIdFrom, uniqueIdTo, amount);

    verify(accountsValidator).validateTransfer(any(TransferValidationPayload.class));
    assertThat(accountFrom.getBalance()).isEqualTo(BigDecimal.ZERO);
    assertThat(accountTo.getBalance()).isEqualTo(amount);

    verify(notificationService)
        .notifyAboutTransfer(
            accountTo,
            String.format("Transfer from account %s, quantity: %s", accountFrom, amount));
    verify(notificationService)
        .notifyAboutTransfer(
            accountFrom, String.format("Transfer to account %s, quantity: %s", accountTo, amount));
  }
}
