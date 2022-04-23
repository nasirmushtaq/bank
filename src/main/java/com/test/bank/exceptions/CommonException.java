package com.test.bank.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CommonException extends RuntimeException {
    private String errorCode;
    private String description;
    private String errorMessage;

    @JsonIgnore
    public Map<String, Object> convertToMap() {
        return Map.of("errorCode", errorCode,
                "description", description,
                "errorMessage", errorMessage);
    }
}
