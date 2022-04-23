package com.test.bank.service;

import com.test.bank.dto.CurrencyType;
import com.test.bank.dto.DomesticTransferDto;
import com.test.bank.dto.InternationTransferDto;
import com.test.bank.dto.TransactionStatus;
import com.test.bank.entity.Account;
import com.test.bank.entity.TransactionDetails;
import com.test.bank.exceptions.CommonException;
import com.test.bank.repository.TransactionRepository;
import com.test.bank.validations.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FxService fxService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletService walletService;

    @Transactional
    public TransactionDetails saveTransaction(TransactionDetails transactionDetails) {
        try {
            Optional<TransactionDetails> existingTransaction = transactionRepository.findByTransactionId(transactionDetails.getTransactionId());
            if (existingTransaction.isEmpty()) {
                return transactionRepository.insert(transactionDetails);
            } else {
                return transactionRepository.update(transactionDetails);
            }
        } catch (CommonException e) {
            log.error("[TransactionService::saveTransaction] failed to save transaction:{}, msg:{}, ex:{}", transactionDetails, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[TransactionService::saveTransaction] failed to save transaction:{}, msg:{}, ex:{}", transactionDetails, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }

    public Optional<TransactionDetails> findByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }

    @Transactional
    public void internationalTransfer(InternationTransferDto internationTransferDto) {
        try {
            Validator.validateInternationalTransferDto(internationTransferDto);
            //lock users
            Optional<Account> sourceAccount = accountService.findByUserId(internationTransferDto.getSourceUser());
            Optional<Account> targetAccount = accountService.findByUserId(internationTransferDto.getDestUser());
            if (sourceAccount.isEmpty() || targetAccount.isEmpty()) {
                throw CommonException.builder().errorCode("invalid_account").build();
            }
            TransactionDetails transactionDetails = createTransactionRequest(internationTransferDto.getSourceUser(), internationTransferDto.getDestUser(), internationTransferDto.getSourceCurrency(), internationTransferDto.getTargetCurrency(), internationTransferDto.getSourceAmount());
            walletService.withdrawFromWallet(internationTransferDto.getSourceUser(), internationTransferDto.getSourceAmount(), internationTransferDto.getSourceCurrency());
            walletService.depositToWallet(internationTransferDto.getDestUser(), transactionDetails.getTargetAmount(), internationTransferDto.getTargetCurrency());
            saveTransaction(transactionDetails);
        } catch (CommonException e) {
            log.error("[TransactionService::transact] failed to transact :{} with amount:{}, msg:{}, ex:{}", internationTransferDto, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[TransactionService::transact] failed to transact :{} with amount:{}, msg:{}, ex:{}", internationTransferDto, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }

    @Transactional
    public void domesticTransfer(DomesticTransferDto domesticTransferDto) {
        try {
            Validator.validateDomesticTransferDto(domesticTransferDto);
            //lock users
            Optional<Account> sourceAccount = accountService.findByUserId(domesticTransferDto.getSourceUser());
            Optional<Account> targetAccount = accountService.findByUserId(domesticTransferDto.getDestUser());
            if (sourceAccount.isEmpty() || targetAccount.isEmpty()) {
                throw CommonException.builder().errorCode("invalid_account").build();
            }
            TransactionDetails transactionDetails = createTransactionRequest(domesticTransferDto.getSourceUser(), domesticTransferDto.getDestUser(), domesticTransferDto.getCurrency(), domesticTransferDto.getCurrency(), domesticTransferDto.getSourceAmount());
            walletService.withdrawFromWallet(domesticTransferDto.getSourceUser(), domesticTransferDto.getSourceAmount(), domesticTransferDto.getCurrency());
            walletService.depositToWallet(domesticTransferDto.getDestUser(), transactionDetails.getTargetAmount(), domesticTransferDto.getCurrency());
            saveTransaction(transactionDetails);
        } catch (CommonException e) {
            log.error("[TransactionService::transact] failed to transact :{} with amount:{}, msg:{}, ex:{}", domesticTransferDto, e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("[TransactionService::transact] failed to transact :{} with amount:{}, msg:{}, ex:{}", domesticTransferDto, e.getMessage(), e);
            throw CommonException.builder().errorCode("server_error").build();
        }
    }

    private TransactionDetails createTransactionRequest(String sourceUser, String destUser, CurrencyType sourceCurrency, CurrencyType destCurrency, double sourceAmount) {
        TransactionDetails.TransactionDetailsBuilder transactionDetails = TransactionDetails.builder();
        transactionDetails.transactionId(UUID.randomUUID().toString());
        double rate = 1.0;
        transactionDetails.rate(rate);
        if (sourceCurrency == destCurrency) {
            transactionDetails.targetAmount(sourceAmount);

        } else {
            rate = fxService.convert(sourceCurrency, destCurrency);
            transactionDetails.rate(rate);
            transactionDetails.targetAmount(rate * sourceAmount);
        }
        transactionDetails.sourceAmount(sourceAmount);
        transactionDetails.sourceCurrency(sourceCurrency);
        transactionDetails.targetCurrency(destCurrency);
        transactionDetails.sourceUserId(sourceUser);
        transactionDetails.targetUserId(destUser);
        transactionDetails.status(TransactionStatus.complete);
        return transactionDetails.build();
    }

}
