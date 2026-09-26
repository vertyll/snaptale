package com.vertyll.snaptale.post;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

@Getter
@Entity
@Table(name = "comments")
class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "post_id", nullable = false)
    private long postId;

    @Column(name = "user_id", nullable = false)
    private long authorId;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentEntity() {
        this.text = "";
        this.createdAt = Instant.EPOCH;
    }

    CommentEntity(long postId, long authorId, String text, Instant createdAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.text = text;
        this.createdAt = createdAt;
    }

    long requireId() {
        if (id == null) {
            throw new IllegalStateException("Comment has not been saved yet");
        }
        return id;
    }
}
