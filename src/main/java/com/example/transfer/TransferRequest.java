package com.example.transfer;

import java.math.BigDecimal;
import java.util.Currency;

public record TransferRequest(String fromAccountId, String toAccountId,
                              BigDecimal amount, Currency currency) {
}
