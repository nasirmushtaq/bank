package com.test.bank.repository;

import com.test.bank.entity.Account;
import com.test.bank.exceptions.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
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
public class AccountRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private Map<String, String> queryMap;

    final String ALL_COLUMNS = "id, user_id ,status, nick_name,routing_no,name,account_no,email, phone_no, deleted, created_at, updated_at";
    final String INSERT_COLUMNS = "user_id ,status, nick_name,routing_no,name,account_no,email, phone_no, deleted";
    final String UPDATE_COLUMNS = "country ,status, nick_name,routing_no,name,account_no,email, phone_no, deleted";
    final String UPDATE_VALUE_NAMES = Arrays.stream(UPDATE_COLUMNS.split(",")).map(s -> s.trim() + "=:" + s.trim()).collect(Collectors.joining(", "));
    final String INSERT_VALUE_NAMES = Arrays.stream(INSERT_COLUMNS.split(",")).map(s -> ":" + s.trim()).collect(Collectors.joining(", "));

    @PostConstruct
    public void initQueryMap() {
        queryMap = new HashMap<>();
        queryMap.put("INSERT_QUERY", "insert into account(" + INSERT_COLUMNS + ") values(" + INSERT_VALUE_NAMES + ")");
        queryMap.put("UPDATE_QUERY", "update account set(" + UPDATE_VALUE_NAMES + ") where user_id=:user_id and account_no=:account_no and ifsc_code=:ifsc_code");
        queryMap.put("DELETE_ACCOUNT", "update account set deleted=:deleted where user_id=:user_id and account_no=:account_no and routing_no=:routing_no and deleted=false");
        queryMap.put("FETCH_ACCOUNT_BY_ACCOUNT_NO", "select " + ALL_COLUMNS + " from account where user_id=:user_id and account_no=:account_no and deleted=:deleted  for update");
        queryMap.put("FETCH_ACCOUNT_BY_USER", "select " + ALL_COLUMNS + " from account where user_id=:user_id and deleted=:deleted  for update");
        queryMap.put("FIND_BY_EMAIL", "select " + ALL_COLUMNS + " from account where email=:email and deleted=:deleted ");
        queryMap.put("FIND_ACCOUNTS_BY_PAGE", "select " + ALL_COLUMNS + " from account where deleted=false order by created_at limit :offset, :limit");
    }

    public Account insert(Account userAccount) {
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("user_id", userAccount.getUserId())
                    .addValue("nick_name", userAccount.getNickName())
                    .addValue("routing_no", userAccount.getRoutingNo())
                    .addValue("name", userAccount.getName())
                    .addValue("account_no", userAccount.getAccountNo())
                    .addValue("email", userAccount.getEmail())
                    .addValue("phone_no", userAccount.getPhoneNo())
                    .addValue("deleted", false)
                    .addValue("status", userAccount.getStatus().name());
            int result = jdbcTemplate.update(queryMap.get("INSERT_QUERY"), parameters, holder);
            userAccount.setId(BigInteger.valueOf(holder.getKey().longValue()));
            return userAccount;
        } catch (DuplicateKeyException e) {
            log.error("[AccountRepository::insert] sql exception while updating: {}, ex: {}", userAccount, e);
            throw CommonException.builder().errorCode("duplicate_request").errorMessage("account already exists").build();
        } catch (Exception ex) {
            log.error("[AccountRepository::insert] sql exception while inserting: {}, ex: {}", userAccount, ex);
            throw CommonException.builder().errorCode("account_save_failed").errorMessage("Failed to save account details").build();
        }
    }

    public int deleteAccount(String userId, String accountNo, String routingCode) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("routing_no", routingCode);
            map.put("user_id", userId);
            map.put("account_no", accountNo);
            map.put("deleted", true);
            int result = jdbcTemplate.update(queryMap.get("DELETE_ACCOUNT"), map);
            log.info("[AccountRepository::deleteAccount] successfully deleted account:{} for user:{} ", accountNo, userId);
            return result;
        } catch (Exception ex) {
            log.error("[AccountRepository::deleteAccount] sql exception while deleting account: {}, msg:{}, ex: {}", accountNo, ex.getMessage(), ex);
            throw CommonException.builder().errorCode("delete_account_failed").errorMessage("account deleted failed").build();
        }
    }

    public Optional<Account> findByAccountNoAndUser(String userId, String accountNo) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("user_id", userId);
            map.put("account_no", accountNo);
            map.put("deleted", false);
            List<Account> accountList = jdbcTemplate.query(queryMap.get("FETCH_ACCOUNT_BY_ACCOUNT_NO"), map, BeanPropertyRowMapper.newInstance(Account.class));
            log.info("[AccountRepository::findByAccountNoAndUser] fetched account:{} for user:{} ", accountList, userId);
            return accountList.isEmpty() ? Optional.empty() : Optional.of(accountList.get(0));
        } catch (Exception ex) {
            log.error("[AccountRepository::findByAccountNoAndUser] sql exception while fetching account: {}, msg:{}, ex: {}", accountNo, ex.getMessage(), ex);
            throw CommonException.builder().errorCode("fetch_account_failed").errorMessage("account fetch failed").build();
        }
    }

    public Optional<Account> findByUser(String userId) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("user_id", userId);
            map.put("deleted", false);
            List<Account> accountList = jdbcTemplate.query(queryMap.get("FETCH_ACCOUNT_BY_USER"), map, BeanPropertyRowMapper.newInstance(Account.class));
            log.info("[AccountRepository::findByUser] fetched account:{} for user:{} ", accountList, userId);
            return accountList.isEmpty() ? Optional.empty() : Optional.of(accountList.get(0));
        } catch (Exception ex) {
            log.error("[AccountRepository::findByUser] sql exception while fetching account for user: {}, msg:{}, ex: {}", userId, ex.getMessage(), ex);
            throw CommonException.builder().errorCode("fetch_account_failed").errorMessage("account fetch failed").build();
        }
    }

    public Optional<Account> findByEmail(String email) {
        Map<String, Object> map = new HashMap<>();
        Optional<Account> account = Optional.empty();
        try {
            map.put("email", email);
            map.put("deleted", false);
            List<Account> accountList = jdbcTemplate.query(queryMap.get("FIND_BY_EMAIL"), map, BeanPropertyRowMapper.newInstance(Account.class));
            log.info("[AccountRepository::findByEmail] fetched account:{} for email:{} ", accountList, email);
            return accountList.isEmpty() ? Optional.empty() : Optional.of(accountList.get(0));
        } catch (Exception ex) {
            log.error("[AccountRepository::findByEmail] sql exception while fetching account: {}, msg:{}, ex: {}", account, ex.getMessage(), ex);
            throw CommonException.builder().errorCode("fetch_account_email_failed").errorMessage("account fetch failed").build();
        }
    }

    public List<Account> getAccounts(int pageNo) {
        int pageSize = 10;
        int offset = pageNo == 0 ? 0 : pageNo * pageSize + 1;
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("limit", pageSize);
            map.put("offset", offset);
            List<Account> paymentDtoList = jdbcTemplate.query(queryMap.get("FIND_ACCOUNTS_BY_PAGE"), map, BeanPropertyRowMapper.newInstance(Account.class));
            return paymentDtoList;
        } catch (Exception e) {
            log.error("[AccountRepository::getAccounts] sql exception failed to get accounts for page:{}, msg:{}, ex:{}", pageNo, e.getMessage(), e);
            throw CommonException.builder()
                    .errorCode("fetch_accounts_failed")
                    .build();
        }
    }
}
