package com.db.awmd.challenge.config;

import com.db.awmd.challenge.service.AccountsValidator;
import com.db.awmd.challenge.service.NotificationService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class AccountServiceTestConfig {

  @Bean
  @Primary
  public NotificationService notificationService() {
    return Mockito.mock(NotificationService.class);
  }

  @Bean
  @Primary
  public AccountsValidator accountsValidator() {
    return Mockito.mock(AccountsValidator.class);
  }
}
