package com.vertyll.snaptale.user;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.snaptale.common.ConflictException;
import com.vertyll.snaptale.common.MessageKeys;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccounts {

    private final UserRepository repository;
    private final UserDirectory directory;
    private final Clock clock;

    public Account register(String name, String email, String passwordHash) {
        String normalized = Emails.normalize(email);
        if (repository.existsByEmail(normalized)) {
            throw new ConflictException(MessageKeys.AUTH_EMAIL_TAKEN);
        }
        return Account.of(repository.save(new UserEntity(name.strip(), normalized, passwordHash, clock.instant())));
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        return repository.findByEmail(Emails.normalize(email)).map(Account::of);
    }

    @Transactional(readOnly = true)
    public Account get(long userId) {
        return Account.of(directory.get(userId));
    }

    public void changePassword(long userId, String passwordHash) {
        directory.get(userId).changePassword(passwordHash, clock.instant());
    }

    public void markEmailVerified(long userId) {
        directory.get(userId).markEmailVerified(clock.instant());
    }
}
