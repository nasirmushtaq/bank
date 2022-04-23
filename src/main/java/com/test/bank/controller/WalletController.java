package com.test.bank.controller;

import com.test.bank.dto.WalletDto;
import com.test.bank.exceptions.CommonException;
import com.test.bank.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody WalletDto walletDto) {
        try {
            return ResponseEntity.ok(walletService.depositToWallet(walletDto.getUserId(), walletDto.getAmount(), walletDto.getCurrency()));
        } catch (CommonException e) {
            return ResponseEntity.badRequest().body(e.convertToMap());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WalletDto walletDto) {
        try {
            return ResponseEntity.ok(walletService.withdrawFromWallet(walletDto.getUserId(), walletDto.getAmount(), walletDto.getCurrency()));
        } catch (CommonException e) {
            return ResponseEntity.badRequest().body(e.convertToMap());
        }
    }

    @PostMapping("/checkBalance")
    public ResponseEntity<?> checkBalance(@RequestBody WalletDto walletDto) {
        try {
            return ResponseEntity.ok(walletService.getBalance(walletDto.getUserId(), walletDto.getCurrency()));
        } catch (CommonException e) {
            return ResponseEntity.badRequest().body(e.convertToMap());
        }
    }
}
