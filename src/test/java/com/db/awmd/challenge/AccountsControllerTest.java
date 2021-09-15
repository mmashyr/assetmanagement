package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.MoneyTransfer;
import com.db.awmd.challenge.service.AccountsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private MockMvc mockMvc;
  @Autowired private AccountsService accountsService;
  @Autowired private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}"))
        .andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}"))
        .andExpect(status().isCreated());

    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":1000}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc
        .perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":-1000}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc
        .perform(
            post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":1000}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc
        .perform(get("/v1/accounts/" + uniqueAccountId))
        .andExpect(status().isOk())
        .andExpect(
            content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  public void validTransfer() throws Exception {
    String transferAmount = "123.45";

    String accountIdFrom = "IdTo-" + System.currentTimeMillis();
    Account accountFrom = new Account(accountIdFrom, new BigDecimal(transferAmount));
    this.accountsService.createAccount(accountFrom);

    String accountIdTo = "IdFrom-" + System.currentTimeMillis();
    Account accountTo = new Account(accountIdTo, BigDecimal.ZERO);
    this.accountsService.createAccount(accountTo);

    MoneyTransfer transfer =
        MoneyTransfer.builder()
            .accountIdFrom(accountIdFrom)
            .accountIdTo(accountIdTo)
            .amount(new BigDecimal(transferAmount))
            .build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isOk());
  }

  @Test
  public void createTransferEmptyTransfer() throws Exception {
    MoneyTransfer transfer = MoneyTransfer.builder().build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferNegativeAmount() throws Exception {
    String accountIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(accountIdFrom, new BigDecimal("123.45"));
    this.accountsService.createAccount(accountFrom);

    String accountIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(accountIdTo, BigDecimal.ZERO);
    this.accountsService.createAccount(accountTo);

    MoneyTransfer transfer =
        MoneyTransfer.builder()
            .accountIdFrom(accountIdFrom)
            .accountIdTo(accountIdTo)
            .amount(new BigDecimal("-1.00"))
            .build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferEmptyFromAccount() throws Exception {
    String accountIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(accountIdTo, BigDecimal.ZERO);
    this.accountsService.createAccount(accountTo);

    MoneyTransfer transfer =
        MoneyTransfer.builder().accountIdTo(accountIdTo).amount(new BigDecimal("-1.00")).build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferEmptyToAccount() throws Exception {
    String accountIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(accountIdFrom, new BigDecimal("123.45"));
    this.accountsService.createAccount(accountFrom);

    MoneyTransfer transfer =
        MoneyTransfer.builder()
            .accountIdFrom(accountIdFrom)
            .amount(new BigDecimal("-1.00"))
            .build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void transferNotEnoughMoney() throws Exception {
    String transferAmount = "123.45";

    String accountIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(accountIdFrom, BigDecimal.ZERO);
    this.accountsService.createAccount(accountFrom);

    String accountIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(accountIdTo, BigDecimal.ZERO);
    this.accountsService.createAccount(accountTo);

    MoneyTransfer transfer =
        MoneyTransfer.builder()
            .accountIdFrom(accountIdFrom)
            .accountIdTo(accountIdTo)
            .amount(new BigDecimal(transferAmount))
            .build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Insufficient balance to perform transfer"));
  }

  @Test
  public void transferSameAccountNumber() throws Exception {
    String accountIdFrom = "IdFrom-" + System.currentTimeMillis();
    Account accountFrom = new Account(accountIdFrom, BigDecimal.TEN);
    this.accountsService.createAccount(accountFrom);

    String accountIdTo = "IdTo-" + System.currentTimeMillis();
    Account accountTo = new Account(accountIdTo, BigDecimal.ZERO);
    this.accountsService.createAccount(accountTo);

    MoneyTransfer transfer =
        MoneyTransfer.builder()
            .accountIdFrom(accountIdFrom)
            .accountIdTo(accountIdFrom)
            .amount(BigDecimal.ONE)
            .build();

    String content = OBJECT_MAPPER.writeValueAsString(transfer);
    this.mockMvc
        .perform(
            post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Self transfer is not allowed"));
  }
}
