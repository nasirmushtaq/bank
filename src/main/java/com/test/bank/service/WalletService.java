package com.test.bank.service;

import com.test.bank.dto.CurrencyType;
import com.test.bank.entity.Account;
import com.test.bank.entity.Wallet;
import com.test.bank.exceptions.CommonException;
import com.test.bank.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class WalletService {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private AccountService accountService;

    @Transactional
    public Wallet saveOrUpdateWallet(Wallet wallet) {
        try {
            Optional<Wallet> existingWallet = walletRepository.findByUserAndCurrency(wallet.getUserId(), wallet.getCurrency());
            if (existingWallet.isEmpty()) {
                return walletRepository.insert(wallet);
            } else {
                return walletRepository.update(wallet);
            }
        } catch (CommonException e) {
            log.error("[WalletService::saveOrUpdateWallet] failed to update wallet:{}, msg:{}, ex:{}", wallet, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[WalletService::saveOrUpdateWallet] failed to update wallet:{}, msg:{}, ex:{}", wallet, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }

    @Transactional
    public Wallet depositToWallet(String userId, double amount, CurrencyType currencyType) {
        try {
            //lock
            if (amount <= 0) {
                throw CommonException.builder().errorCode("invalid_amount").build();
            }
            Optional<Account> existingAccount = accountService.findByUserId(userId);
            if (existingAccount.isEmpty()) {
                throw CommonException.builder().errorCode("invalid_account_details").build();
            }
            Optional<Wallet> existingWallet = walletRepository.findByUserAndCurrency(userId, currencyType);
            if (existingWallet.isEmpty()) {
                return walletRepository.insert(Wallet.builder().amount(amount).currency(currencyType).userId(userId).build());
            }
            existingWallet.get().setAmount(existingWallet.get().getAmount() + amount);
            return walletRepository.update(existingWallet.get());

        } catch (CommonException e) {
            log.error("[WalletService::depositToWallet] failed to deposit wallet:{}, msg:{}, ex:{}", userId, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[WalletService::depositToWallet] failed to deposit wallet:{}, msg:{}, ex:{}", userId, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }

    @Transactional
    public Wallet withdrawFromWallet(String userId, double amount, CurrencyType currencyType) {
        try {
            if (amount <= 0) {
                throw CommonException.builder().errorCode("invalid_amount").build();
            }
            Optional<Account> existingAccount = accountService.findByUserId(userId);
            if (existingAccount.isEmpty()) {
                throw CommonException.builder().errorCode("invalid_account_details").build();
            }
            Optional<Wallet> existingWallet = walletRepository.findByUserAndCurrency(userId, currencyType);
            if (existingWallet.isEmpty() || existingWallet.get().getAmount() < amount) {
                throw CommonException.builder().errorCode("insufficient_balance").errorMessage("wallet not created or amount insufficient").build();
            }
            existingWallet.get().setAmount(existingWallet.get().getAmount() - amount);
            return walletRepository.update(existingWallet.get());
        } catch (CommonException e) {
            log.error("[WalletService::saveOrUpdateWallet] failed to withdraw wallet:{}, msg:{}, ex:{}", amount, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[WalletService::saveOrUpdateWallet] failed to withdraw wallet:{}, msg:{}, ex:{}", amount, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }

    @Transactional
    public Wallet getBalance(String userId, CurrencyType currencyType) {
        try {
            Optional<Account> existingAccount = accountService.findByUserId(userId);
            if (existingAccount.isEmpty()) {
                throw CommonException.builder().errorCode("account_not_found").errorMessage("account not created").build();
            }
            Optional<Wallet> existingWallet = walletRepository.findByUserAndCurrency(userId, currencyType);
            if (existingWallet.isEmpty()) {
                throw CommonException.builder().errorCode("wallet_not_found").errorMessage("wallet not created").build();
            }
            return existingWallet.get();
        } catch (CommonException e) {
            log.error("[WalletService::saveOrUpdateWallet] failed to fetch wallet:{}, msg:{}, ex:{}", userId, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[WalletService::saveOrUpdateWallet] failed to fetch wallet:{}, msg:{}, ex:{}", userId, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }
}
