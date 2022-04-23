package com.test.bank.validations;

import com.test.bank.dto.AccountDto;
import com.test.bank.dto.DomesticTransferDto;
import com.test.bank.dto.InternationTransferDto;
import com.test.bank.exceptions.CommonException;
import org.springframework.util.Assert;

public class Validator {
    public static boolean validateAccountDto(AccountDto accountDto) {
        try {
            Assert.notNull(accountDto.getEmail());
            Assert.notNull(accountDto.getName());
            Assert.notNull(accountDto.getPhoneNo());
        } catch (Exception e) {
            throw CommonException.builder().errorCode("invalid_request").build();
        }
        return true;
    }

    public static boolean validateDomesticTransferDto(DomesticTransferDto domesticTransferDto) {
        try {
            Assert.notNull(domesticTransferDto.getSourceUser());
            Assert.notNull(domesticTransferDto.getDestUser());
            Assert.notNull(domesticTransferDto.getCurrency());
            Assert.isTrue(domesticTransferDto.getSourceAmount() > 0);
        } catch (Exception e) {
            throw CommonException.builder().errorCode("invalid_request").build();
        }
        return true;
    }

    public static boolean validateInternationalTransferDto(InternationTransferDto internationTransferDto) {
        try {
            Assert.notNull(internationTransferDto.getSourceUser());
            Assert.notNull(internationTransferDto.getDestUser());
            Assert.notNull(internationTransferDto.getSourceCurrency());
            Assert.notNull(internationTransferDto.getTargetCurrency());
            Assert.isTrue(internationTransferDto.getSourceAmount() > 0);
        } catch (Exception e) {
            throw CommonException.builder().errorCode("invalid_request").build();
        }
        return true;
    }
}
