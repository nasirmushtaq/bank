package com.test.bank.repository;

import com.test.bank.dto.CurrencyType;
import com.test.bank.entity.Account;
import com.test.bank.entity.TransactionDetails;
import com.test.bank.entity.Wallet;
import com.test.bank.exceptions.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class WalletRepository {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private Map<String, String> queryMap;

    final String ALL_COLUMNS = "id, user_id, currency, amount, created_at, updated_at";
    final String INSERT_COLUMNS = "user_id, currency, amount";
    final String UPDATE_COLUMNS = " amount";
    final String UPDATE_VALUE_NAMES = Arrays.stream(UPDATE_COLUMNS.split(",")).map(s -> s.trim() + "=:" + s.trim()).collect(Collectors.joining(", "));
    final String INSERT_VALUE_NAMES = Arrays.stream(INSERT_COLUMNS.split(",")).map(s -> ":" + s.trim()).collect(Collectors.joining(", "));

    @PostConstruct
    public void initQueryMap() {
        queryMap = new HashMap<>();
        queryMap.put("INSERT_QUERY", "insert into wallet(" + INSERT_COLUMNS + ") values(" + INSERT_VALUE_NAMES + ")");
        queryMap.put("UPDATE_QUERY", "update wallet set(" + UPDATE_VALUE_NAMES + ") where user_id=:user_id and currency=:currency");
        queryMap.put("UPDATE_WALLET", "update wallet set amount=:amount where user_id=:user_id and currency=:currency");
        queryMap.put("FIND_BY_USER_ID", "select " + ALL_COLUMNS + " from wallet where user_id=:user_id for update");
        queryMap.put("FIND_BY_USER_ID_AND_CURRENCY", "select " + ALL_COLUMNS + " from wallet where user_id=:user_id and currency=:currency for update");
    }

    public Wallet insert(Wallet wallet) {
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("user_id", wallet.getUserId())
                    .addValue("currency", wallet.getCurrency().name())
                    .addValue("amount", wallet.getAmount());
            int result = jdbcTemplate.update(queryMap.get("INSERT_QUERY"), parameters, holder);
            wallet.setId(BigInteger.valueOf(holder.getKey().longValue()));
            return wallet;
        } catch (DuplicateKeyException e) {
            log.error("[WalletRepository::insert] sql exception while inserting: {}, ex: {}", wallet, e);
            throw CommonException.builder().errorCode("duplicate_request").errorMessage("wallet already exists").build();
        } catch (Exception ex) {
            log.error("[WalletRepository::insert] sql exception while inserting: {}, ex: {}", wallet, ex);
            throw CommonException.builder().errorCode("wallet_save_failed").errorMessage("Failed to save transaction details").build();
        }
    }

    public Wallet update(Wallet wallet) {
        try {
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("user_id", wallet.getUserId())
                    .addValue("currency", wallet.getCurrency().name())
                    .addValue("amount", wallet.getAmount());
            int result = jdbcTemplate.update(queryMap.get("UPDATE_WALLET"), parameters);
            return wallet;
        } catch (Exception ex) {
            log.error("[WalletRepository::update] sql exception while updating: {}, ex: {}", wallet, ex);
            throw CommonException.builder().errorCode("wallet_update_failed").errorMessage("Failed to update wallet").build();
        }
    }

    /*public int updateAmount(String userId, double amount, CurrencyType currencyType) {
        try {
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("user_id", userId)
                    .addValue("currency", currencyType.name())
                    .addValue("amount", amount);
            int result = jdbcTemplate.update(queryMap.get("UPDATE_QUERY"), parameters);
            return result;
        } catch (Exception ex) {
            log.error("[WalletRepository::update] sql exception while updating: {}, ex: {}", wallet, ex);
            throw CommonException.builder().errorCode("wallet_update_failed").errorMessage("Failed to update wallet").build();
        }
    }*/


    public Optional<Wallet> findByUserAndCurrency(String userId, CurrencyType currencyType) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("user_id", userId);
            map.put("currency", currencyType.name());
            List<Wallet> wallets = jdbcTemplate.query(queryMap.get("FIND_BY_USER_ID_AND_CURRENCY"), map, BeanPropertyRowMapper.newInstance(Wallet.class));
            log.info("[WalletRepository::findByUserAndCurrency] fetched wallet:{} for user:{} ", wallets, userId);
            return wallets.isEmpty() ? Optional.empty() : Optional.of(wallets.get(0));
        } catch (Exception ex) {
            log.error("[WalletRepository::findByUserAndCurrency] sql exception while fetching wallet: {}, msg:{}, ex: {}", userId, ex.getMessage(), ex);
            throw CommonException.builder().errorCode("fetch_wallet_failed").errorMessage("wallet fetch failed").build();
        }
    }
}
