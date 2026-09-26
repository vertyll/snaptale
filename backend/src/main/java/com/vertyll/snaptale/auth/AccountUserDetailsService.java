package com.vertyll.snaptale.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vertyll.snaptale.security.AuthenticatedUser;
import com.vertyll.snaptale.user.UserAccounts;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class AccountUserDetailsService implements UserDetailsService {

    private final UserAccounts accounts;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return accounts.findByEmail(email)
            .map(account -> new AuthenticatedUser(account.id(), account.email(), account.passwordHash()))
            .orElseThrow(() -> new UsernameNotFoundException("No account for the given e-mail"));
    }
}
