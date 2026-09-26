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
@Table(name = "posts")
class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "user_id", nullable = false)
    private long authorId;

    @Column(nullable = false)
    private String text;

    @Column(name = "video_path", nullable = false)
    private String videoPath;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostEntity() {
        this.text = "";
        this.videoPath = "";
        this.createdAt = Instant.EPOCH;
    }

    PostEntity(long authorId, String text, String videoPath, Instant createdAt) {
        this.authorId = authorId;
        this.text = text;
        this.videoPath = videoPath;
        this.createdAt = createdAt;
    }

    long requireId() {
        if (id == null) {
            throw new IllegalStateException("Post has not been saved yet");
        }
        return id;
    }
}
