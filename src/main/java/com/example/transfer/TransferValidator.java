package com.example.transfer;

import com.example.transfer.error.InvalidTransferException;
import com.example.transfer.error.TransferError;

import java.math.BigDecimal;
import java.util.Currency;

public class TransferValidator {
    private final AccountRepository accounts;
    private final TransferLimitProvider limits;

    public TransferValidator(AccountRepository accounts, TransferLimitProvider limits) {
        this.accounts = accounts;
        this.limits = limits;
    }

    public void validate(TransferRequest request) {
        if(request == null || request.amount() == null || request.currency() == null || request.fromAccountId() == null || request.toAccountId() == null)
            throw new InvalidTransferException(TransferError.INVALID_REQUEST);
        if(request.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidTransferException(TransferError.AMOUNT_NOT_POSITIVE);

        if (request.amount().scale() > request.currency().getDefaultFractionDigits())
            throw new InvalidTransferException(TransferError.AMOUNT_SCALE_INVALID);

        if(request.fromAccountId().equals(request.toAccountId()))
            throw new InvalidTransferException(TransferError.SAME_ACCOUNT);

        Account from = accounts.findById(request.fromAccountId())
                .orElseThrow(() -> new InvalidTransferException(TransferError.ACCOUNT_NOT_FOUND));
        BigDecimal balance = from.balance();
        Currency currencyFrom = from.currency();

        Account to = accounts.findById(request.toAccountId())
                .orElseThrow(() -> new InvalidTransferException(TransferError.ACCOUNT_NOT_FOUND));
        Currency currencyTo = to.currency();

        if (!request.currency().equals(currencyFrom) || !request.currency().equals(currencyTo))
            throw new InvalidTransferException(TransferError.CURRENCY_MISMATCH);

        if(limits.getLimit(request.fromAccountId()).compareTo(request.amount()) < 0)
            throw new InvalidTransferException(TransferError.LIMIT_EXCEEDED);

        if(balance.compareTo(request.amount()) < 0)
            throw new InvalidTransferException(TransferError.INSUFFICIENT_FUNDS);

    }
}
