package com.example.servermaintenance.account;

import lombok.AllArgsConstructor;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.Permission;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Transactional
    public void syncAccount(AccessToken token) throws AccountNotFoundException {
        var uuid = UUID.fromString(token.getId());
        var account = accountRepository.findAccountByKeyCloakUuid(uuid).orElse(new Account());
        account.setEmail(token.getEmail());
        account.setFirstName(token.getName());
        account.setLastName(token.getFamilyName());
        account.setRoles(token.getRealmAccess().getRoles());
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) accountRepository.findFirstByEmail(username).orElseThrow();
    }

    public Account getAccountById(int accountId) {
        return accountRepository.getById((long) accountId);
    }

    public boolean isAdmin(Account account) {
        return account.getRoles().contains("ADMIN");
    }
}
