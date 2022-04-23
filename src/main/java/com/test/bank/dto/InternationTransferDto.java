package com.test.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InternationTransferDto {
    private String sourceUser;
    private String destUser;
    private CurrencyType sourceCurrency;
    private CurrencyType targetCurrency;
    private double sourceAmount;
}
