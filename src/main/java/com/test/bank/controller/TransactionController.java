package com.test.bank.controller;

import com.test.bank.dto.DomesticTransferDto;
import com.test.bank.dto.InternationTransferDto;
import com.test.bank.exceptions.CommonException;
import com.test.bank.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@Slf4j
@RequestMapping("/v1/transfer")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @RequestMapping("/domesticTransfer")
    public ResponseEntity<?> domesticTransfer(@RequestBody DomesticTransferDto domesticTransferDto) {
        try {
            transactionService.domesticTransfer(domesticTransferDto);
            return ResponseEntity.ok("success");
        } catch (CommonException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/internationalTransfer")
    public ResponseEntity<?> internationalTransfer(@RequestBody InternationTransferDto internationTransferDto) {
        try {
            transactionService.internationalTransfer(internationTransferDto);
            return ResponseEntity.ok("success");
        } catch (CommonException e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }
}
