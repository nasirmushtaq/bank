package com.test.bank.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CommonException extends RuntimeException {
    private String errorCode;
    private String description;
    private String errorMessage;
}
