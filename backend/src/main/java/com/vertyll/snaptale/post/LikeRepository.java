package com.vertyll.snaptale.post;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface LikeRepository extends JpaRepository<LikeEntity, LikeId> {

    @Query("""
            SELECT new com.vertyll.snaptale.post.PostCount(l.id.postId, COUNT(l))
            FROM LikeEntity l WHERE l.id.postId IN :postIds GROUP BY l.id.postId""")
    List<PostCount> countByPostIds(Collection<Long> postIds);

    @Query("SELECT l.id.postId FROM LikeEntity l WHERE l.id.userId = :userId AND l.id.postId IN :postIds")
    List<Long> findLikedPostIds(long userId, Collection<Long> postIds);
}
