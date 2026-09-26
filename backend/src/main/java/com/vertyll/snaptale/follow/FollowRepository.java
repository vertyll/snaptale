package com.vertyll.snaptale.follow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface FollowRepository extends JpaRepository<FollowEntity, FollowId> {

    long countByIdFollowedId(long followedId);

    long countByIdFollowerId(long followerId);

    @Query("SELECT f.id.followedId FROM FollowEntity f WHERE f.id.followerId = :followerId ORDER BY f.createdAt DESC")
    List<Long> findFollowedIds(long followerId);
}
