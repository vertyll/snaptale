package com.vertyll.snaptale.post;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;

@Getter
@Entity
@Table(name = "likes")
class LikeEntity {

    @EmbeddedId
    private LikeId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LikeEntity() {
        this.id = new LikeId(0, 0);
        this.createdAt = Instant.EPOCH;
    }

    LikeEntity(LikeId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
