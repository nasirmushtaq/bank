package com.test.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DomesticTransferDto {
    private String sourceUser;
    private String destUser;
    private CurrencyType currency;
    private double sourceAmount;
}
