package com.test.bank.repository;

import com.test.bank.entity.Account;
import com.test.bank.entity.TransactionDetails;
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
public class TransactionRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private Map<String, String> queryMap;

    final String ALL_COLUMNS = "id, source_user_id, target_user_id, transaction_id, status, source_currency, target_currency, source_amount, target_amount, rate, created_at, updated_at";
    final String INSERT_COLUMNS = "source_user_id, target_user_id, transaction_id, status, source_currency, target_currency, source_amount, target_amount, rate";
    final String UPDATE_COLUMNS = "source_user_id, target_user_id, status, source_currency, target_currency, source_amount, target_amount, rate";
    final String UPDATE_VALUE_NAMES = Arrays.stream(UPDATE_COLUMNS.split(",")).map(s -> s.trim() + "=:" + s.trim()).collect(Collectors.joining(", "));
    final String INSERT_VALUE_NAMES = Arrays.stream(INSERT_COLUMNS.split(",")).map(s -> ":" + s.trim()).collect(Collectors.joining(", "));

    @PostConstruct
    public void initQueryMap() {
        queryMap = new HashMap<>();
        queryMap.put("INSERT_QUERY", "insert into transaction_details(" + INSERT_COLUMNS + ") values(" + INSERT_VALUE_NAMES + ")");
        queryMap.put("UPDATE_QUERY", "update transaction_details set(" + UPDATE_VALUE_NAMES + ") where transaction_id=:transaction_id");
        queryMap.put("FIND_BY_TRANSACTION_ID", "select " + ALL_COLUMNS + " from transaction_details where transaction_id=:transaction_id for update");
    }

    public TransactionDetails insert(TransactionDetails transactionDetails) {
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("source_user_id", transactionDetails.getSourceUserId())
                    .addValue("target_user_id", transactionDetails.getTargetUserId())
                    .addValue("transaction_id", transactionDetails.getTransactionId())
                    .addValue("status", transactionDetails.getStatus().name())
                    .addValue("source_currency", transactionDetails.getSourceCurrency().name())
                    .addValue("target_currency", transactionDetails.getTargetCurrency().name())
                    .addValue("source_amount", transactionDetails.getSourceAmount())
                    .addValue("target_amount", transactionDetails.getTargetAmount())
                    .addValue("rate", transactionDetails.getRate());
            int result = jdbcTemplate.update(queryMap.get("INSERT_QUERY"), parameters, holder);
            transactionDetails.setId(BigInteger.valueOf(holder.getKey().longValue()));
            return transactionDetails;
        } catch (DuplicateKeyException e) {
            log.error("[TransactionRepository::insert] sql exception while inserting: {}, ex: {}", transactionDetails, e);
            throw CommonException.builder().errorCode("duplicate_request").errorMessage("transaction already exists").build();
        } catch (Exception ex) {
            log.error("[TransactionRepository::insert] sql exception while inserting: {}, ex: {}", transactionDetails, ex);
            throw CommonException.builder().errorCode("transaction_save_failed").errorMessage("Failed to save transaction details").build();
        }
    }

    public TransactionDetails update(TransactionDetails transactionDetails) {
        try {
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("source_user_id", transactionDetails.getSourceUserId())
                    .addValue("target_user_id", transactionDetails.getTargetUserId())
                    .addValue("transaction_id", transactionDetails.getTransactionId())
                    .addValue("status", transactionDetails.getStatus().name())
                    .addValue("source_currency", transactionDetails.getSourceCurrency().name())
                    .addValue("target_currency", transactionDetails.getTargetCurrency().name())
                    .addValue("source_amount", transactionDetails.getSourceAmount())
                    .addValue("target_amount", transactionDetails.getTargetAmount())
                    .addValue("rate", transactionDetails.getRate());
            int result = jdbcTemplate.update(queryMap.get("UPDATE_QUERY"), parameters);
            return transactionDetails;
        } catch (Exception ex) {
            log.error("[TransactionRepository::update] sql exception while updating: {}, ex: {}", transactionDetails, ex);
            throw CommonException.builder().errorCode("transaction_save_failed").errorMessage("Failed to save transaction details").build();
        }
    }


    public Optional<TransactionDetails> findByTransactionId(String transactionId) {
        Map<String, Object> map = new HashMap<>();
        Optional<Account> account = Optional.empty();
        try {
            map.put("transaction_id", transactionId);
            List<TransactionDetails> transactionDetails = jdbcTemplate.query(queryMap.get("FIND_BY_TRANSACTION_ID"), map, BeanPropertyRowMapper.newInstance(TransactionDetails.class));
            log.info("[AccountRepository::findByTransactionId] fetched transaction:{} for transactionId:{} ", transactionDetails, transactionId);
            return transactionDetails.isEmpty() ? Optional.empty() : Optional.of(transactionDetails.get(0));
        } catch (Exception ex) {
            log.error("[AccountRepository::findByTransactionId] sql exception while fetching transaction: {}, msg:{}, ex: {}", account, ex.getMessage(), ex);
            throw CommonException.builder().errorCode("fetch_transaction_failed").errorMessage("transaction fetch failed").build();
        }
    }
}