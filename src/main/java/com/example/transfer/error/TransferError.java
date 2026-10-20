package com.example.transfer.error;

public enum TransferError {
    INVALID_REQUEST,
    AMOUNT_NOT_POSITIVE,
    AMOUNT_SCALE_INVALID,
    SAME_ACCOUNT,
    ACCOUNT_NOT_FOUND,
    LIMIT_EXCEEDED,
    INSUFFICIENT_FUNDS,
    CURRENCY_MISMATCH
}
