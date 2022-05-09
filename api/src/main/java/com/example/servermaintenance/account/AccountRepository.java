package com.example.servermaintenance.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAll();
    Optional<Account> findByEmail(String email);
    List<Account> findAccountsByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
