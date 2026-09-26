package com.vertyll.snaptale;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.vertyll.snaptale.security.AuthenticatedUser;
import com.vertyll.snaptale.user.Account;
import com.vertyll.snaptale.user.UserAccounts;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@Component
public class TestUsers {

    private final UserAccounts accounts;

    TestUsers(UserAccounts accounts) {
        this.accounts = accounts;
    }

    public Account create(String name) {
        return accounts.register(
            name,
            name.toLowerCase(java.util.Locale.ROOT) + "-" + UUID.randomUUID() + "@example.com",
            "{noop}unused"
        );
    }

    public static RequestPostProcessor as(Account account) {
        return user(new AuthenticatedUser(account.id(), account.email(), null));
    }
}
