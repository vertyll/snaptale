package com.vertyll.snaptale.follow;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;

@Getter
@Entity
@Table(name = "follows")
class FollowEntity {

    @EmbeddedId
    private FollowId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FollowEntity() {
        this.id = new FollowId(0, 0);
        this.createdAt = Instant.EPOCH;
    }

    FollowEntity(FollowId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
