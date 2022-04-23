package com.test.bank.controller;

import com.test.bank.dto.AccountDto;
import com.test.bank.exceptions.CommonException;
import com.test.bank.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/v1/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/createAccount")
    public ResponseEntity<?> createNewAccount(@RequestBody AccountDto accountDto) {
        try {
            return ResponseEntity.ok(accountService.saveAccount(accountDto));
        } catch (CommonException e) {
            throw e;
        }
    }

    @PostMapping("/deleteAccount/{userId}")
    public ResponseEntity<?> createNewAccount(@PathVariable("userId") String userId) {
        try {
            accountService.deleteAccount(userId);
            return ResponseEntity.ok("success");
        } catch (CommonException e) {
            throw e;
        }
    }

    @PostMapping("/getAccounts/{pageNo}")
    public ResponseEntity<?> getAccounts(@PathVariable("pageNo") int pageNo) {
        try {
            return ResponseEntity.ok(accountService.getAccountsByPage(pageNo));
        } catch (CommonException e) {
            throw e;
        }
    }
}
