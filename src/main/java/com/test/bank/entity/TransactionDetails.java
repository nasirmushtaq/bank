package com.test.bank.entity;

import com.test.bank.dto.CurrencyType;
import com.test.bank.dto.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "transaction_details")
public class TransactionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private BigInteger id;
    @Column(name = "source_user_id", nullable = false)
    private String sourceUserId;
    @Column(name = "target_user_id", nullable = false)
    private String targetUserId;
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "source_currency", nullable = false)
    private CurrencyType sourceCurrency;
    @Column(name = "target_currency", nullable = false)
    private CurrencyType targetCurrency;
    @Column(name = "source_amount", nullable = false)
    private double sourceAmount;
    @Column(name = "target_amount", nullable = false)
    private double targetAmount;
    @Column(name = "rate", nullable = false)
    private double rate;
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
}
