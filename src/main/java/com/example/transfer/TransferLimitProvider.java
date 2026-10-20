package com.example.transfer;

import java.math.BigDecimal;

public interface TransferLimitProvider {
    BigDecimal getLimit(String accountId);
}
