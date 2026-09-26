package com.vertyll.snaptale.post;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
record LikeId(@Column(name = "post_id") long postId, @Column(name = "user_id") long userId) implements Serializable {
}
