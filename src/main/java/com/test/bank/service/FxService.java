package com.test.bank.service;

import com.test.bank.dto.CurrencyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FxService {
    Map<String, Double> rateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        rateMap.put(CurrencyType.USD.name() + CurrencyType.INR.name(), 76.6);
        rateMap.put(CurrencyType.INR.name() + CurrencyType.USD.name(), 1.0 / 76.6);
    }

    public double convert(CurrencyType sourceCurrency, CurrencyType destCurrency) {
        return rateMap.get(sourceCurrency.name() + destCurrency.name());
    }
}
