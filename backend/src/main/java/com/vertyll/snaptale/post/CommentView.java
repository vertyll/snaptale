package com.vertyll.snaptale.post;

import java.time.Instant;

import com.vertyll.snaptale.user.UserSummary;

record CommentView(long id, String text, Instant createdAt, UserSummary author) {
}
