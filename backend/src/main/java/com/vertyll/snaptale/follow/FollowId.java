package com.vertyll.snaptale.follow;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
record FollowId(@Column(name = "follower_id") long followerId, @Column(name = "followed_id") long followedId)
    implements
    Serializable {
}
