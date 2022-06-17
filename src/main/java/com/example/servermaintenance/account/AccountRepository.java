package com.example.servermaintenance.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("select a from Account a left join fetch a.roles where a.email = :email")
    Optional<Account> findByEmail(String email);
    @Query("select distinct a from Account a left join fetch a.roles where lower(a.email) like lower(concat('%', :email, '%'))")
    List<Account> findAccountsByEmailContainingIgnoreCase(String email);
}
