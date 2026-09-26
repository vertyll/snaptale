package com.vertyll.snaptale.follow;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.snaptale.common.InvalidRequestException;
import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.NotFoundException;
import com.vertyll.snaptale.security.Viewer;
import com.vertyll.snaptale.user.UserDirectory;
import com.vertyll.snaptale.user.UserSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowGraph {

    private final FollowRepository repository;
    private final UserDirectory users;
    private final Clock clock;

    public FollowCounts counts(long userId) {
        return new FollowCounts(repository.countByIdFollowedId(userId), repository.countByIdFollowerId(userId));
    }

    public boolean follows(Viewer viewer, long userId) {
        return viewer.userId().map(followerId -> repository.existsById(new FollowId(followerId, userId))).orElse(false);
    }

    List<UserSummary> followedBy(long followerId) {
        List<Long> ids = repository.findFollowedIds(followerId);
        Map<Long, UserSummary> summaries = users.summaries(ids);
        return ids.stream().map(summaries::get).filter(Objects::nonNull).toList();
    }

    @Transactional
    void follow(long followerId, long followedId) {
        if (followerId == followedId) {
            throw new InvalidRequestException(MessageKeys.FOLLOW_SELF);
        }
        if (!users.exists(followedId)) {
            throw new NotFoundException(MessageKeys.USER_NOT_FOUND);
        }
        FollowId id = new FollowId(followerId, followedId);
        if (!repository.existsById(id)) {
            repository.save(new FollowEntity(id, clock.instant()));
        }
    }

    @Transactional
    void unfollow(long followerId, long followedId) {
        repository.deleteById(new FollowId(followerId, followedId));
    }
}
