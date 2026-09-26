package com.vertyll.snaptale.security;

import java.util.Optional;

public record Viewer(Optional<Long> userId) {

    public static Viewer of(long userId) {
        return new Viewer(Optional.of(userId));
    }

    public boolean is(long otherUserId) {
        return userId.filter(id -> id == otherUserId).isPresent();
    }
}
