package com.test.bank.service;

import com.test.bank.config.AccountConfigs;
import com.test.bank.dto.AccountDto;
import com.test.bank.dto.AccountStatus;
import com.test.bank.entity.Account;
import com.test.bank.exceptions.CommonException;
import com.test.bank.repository.AccountRepository;
import com.test.bank.validations.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AccountService {
    @Autowired
    private AccountConfigs accountConfigs;
    @Autowired
    private AccountRepository accountRepository;

    private static BigInteger accountSequence = new BigInteger("10000");

    @Transactional
    public Account saveAccount(AccountDto accountDto) {
        try {
            Validator.validateAccountDto(accountDto);
            Optional<Account> existingAccount = accountRepository.findByEmail(accountDto.getEmail());
            if (existingAccount.isPresent()) {
                throw CommonException.builder().errorCode("account_already_exist").errorMessage("Account with same email already exist").build();
            }
            Account account = createNewAccountRequest(accountDto);
            return accountRepository.insert(account);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw CommonException.builder().errorCode("server_error").errorMessage("Something went wrong").build();
        }
    }

    @Transactional
    public void deleteAccount(String userId) {
        try {
            Optional<Account> existingAccount = accountRepository.findByUser(userId);
            if (existingAccount.isEmpty()) {
                throw CommonException.builder().errorCode("account_not_found").errorMessage("Account does not exist").build();
            }
            int result = accountRepository.deleteAccount(existingAccount.get().getUserId(), existingAccount.get().getAccountNo(), existingAccount.get().getRoutingNo());
            if (result == 0) {
                throw CommonException.builder().errorCode("delete_failed").errorMessage("Account deletion failed").build();
            }
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw CommonException.builder().errorCode("server_error").errorMessage("Something went wrong").build();
        }
    }

    public Optional<Account> findByUserId(String userId) {
        try {
            return accountRepository.findByUser(userId);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw CommonException.builder().errorCode("server_error").errorMessage("Something went wrong").build();
        }
    }

    private Account createNewAccountRequest(AccountDto accountDto) {
        return Account.builder()
                .accountNo(getNextAccount())
                .routingNo(accountConfigs.getRoutingNo())
                .deleted(false)
                .email(accountDto.getEmail())
                .name(accountDto.getName())
                .nickName(accountDto.getNickName())
                .status(AccountStatus.active)
                .phoneNo(accountDto.getPhoneNo())
                .userId(UUID.randomUUID().toString())
                .build();
    }

    private String getNextAccount() {
        return accountConfigs.getBaseAccountInfo() + accountSequence.add(new BigInteger("1"));
    }

    public List<Account> getAccountsByPage(int page) {
        return accountRepository.getAccounts(page);
    }
}
