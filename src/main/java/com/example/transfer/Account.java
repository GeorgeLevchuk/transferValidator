package com.example.transfer;

import java.math.BigDecimal;
import java.util.Currency;

public record Account(String id, BigDecimal balance, Currency currency) {
}
