package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@AllArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Transactional
    public void syncAccount(AccessToken token) throws AccountNotFoundException {
        var account = accountRepository.findFirstByEmail(token.getEmail()).orElse(new Account());
        account.setEmail(token.getEmail());
        account.setFirstName(token.getGivenName());
        account.setLastName(token.getFamilyName());
        account.setRoles(token.getRealmAccess().getRoles());
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) accountRepository.findFirstByEmail(username).orElseThrow();
    }

    public Account findByEmail(String email) {
        return accountRepository.findFirstByEmail(email).orElse(null);
    }

    public Account getAccountById(int accountId) {
        return accountRepository.getById((long) accountId);
    }

    public boolean isAdmin(Account account) {
        return account.getRoles().contains("ADMIN");
    }
}
