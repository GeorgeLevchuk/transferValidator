package com.example.transfer;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(String id);
}
