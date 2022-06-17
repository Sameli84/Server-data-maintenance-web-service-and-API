package com.example.servermaintenance.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findFirstByEmail(String email);

    Optional<Account> findAccountByKeyCloakUuid(UUID uuid);
}
