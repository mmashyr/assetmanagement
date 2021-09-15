package com.db.awmd.challenge.dto;

import com.db.awmd.challenge.domain.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class TransferValidationPayload {
  private Account accountFrom;
  private String accountFromId;

  private Account accountTo;
  private String accountToId;

  private BigDecimal amount;
}
