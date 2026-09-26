package com.vertyll.snaptale.user;

public record Account(long id, String name, String email, String passwordHash, boolean emailVerified) {

    static Account of(UserEntity user) {
        return new Account(
            user.requireId(),
            user.getName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getEmailVerifiedAt() != null
        );
    }
}
