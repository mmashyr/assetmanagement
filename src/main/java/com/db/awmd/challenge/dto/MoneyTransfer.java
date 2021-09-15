package com.db.awmd.challenge.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
public class MoneyTransfer {

  @NotNull @NotEmpty private final String accountIdFrom;

  @NotNull @NotEmpty private final String accountIdTo;

  @NotNull
  @Min(value = 0, message = "Amount to transfer must be positive.")
  private BigDecimal amount;

  @JsonCreator
  public MoneyTransfer(
      @JsonProperty("accountIdFrom") String accountIdFrom,
      @JsonProperty("accountIdTo") String accountIdTo,
      @JsonProperty("amount") BigDecimal amount) {
    this.accountIdFrom = accountIdFrom;
    this.accountIdTo = accountIdTo;
    this.amount = amount;
  }
}
