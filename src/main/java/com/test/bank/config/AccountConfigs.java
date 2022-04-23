package com.test.bank.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "account-configs")
@Data
@EnableConfigurationProperties
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountConfigs {
    private String routingNo;
    private String baseAccountInfo;
}
