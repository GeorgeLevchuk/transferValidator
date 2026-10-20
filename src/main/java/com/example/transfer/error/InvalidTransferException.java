package com.example.transfer.error;

public class InvalidTransferException extends RuntimeException {
    private final TransferError error;

    public InvalidTransferException(TransferError error) {
        super(error.name());
        this.error = error;
    }

    public TransferError getError() {
        return error;
    }
}