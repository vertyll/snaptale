package com.vertyll.snaptale.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.NotFoundException;
import com.vertyll.snaptale.security.Viewer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDirectory {

    private static final long NO_USER = -1;

    private final UserRepository repository;

    public boolean exists(long userId) {
        return repository.existsById(userId);
    }

    public UserProfile profile(long userId) {
        return UserProfile.of(get(userId));
    }

    public Map<Long, UserSummary> summaries(Collection<Long> userIds) {
        return repository.findAllById(userIds)
            .stream()
            .map(UserSummary::of)
            .collect(Collectors.toUnmodifiableMap(UserSummary::id, Function.identity()));
    }

    public List<UserSummary> suggested(Viewer viewer, int limit) {
        return repository.findRandom(viewer.userId().orElse(NO_USER), limit).stream().map(UserSummary::of).toList();
    }

    UserEntity get(long userId) {
        return repository.findById(userId).orElseThrow(() -> new NotFoundException(MessageKeys.USER_NOT_FOUND));
    }
}
