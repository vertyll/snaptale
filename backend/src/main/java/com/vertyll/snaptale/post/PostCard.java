package com.vertyll.snaptale.post;

import java.time.Instant;

import com.vertyll.snaptale.user.UserSummary;

public record PostCard(
    long id,
    String text,
    String videoUrl,
    Instant createdAt,
    UserSummary author,
    long likeCount,
    long commentCount,
    boolean likedByMe
) {
}
