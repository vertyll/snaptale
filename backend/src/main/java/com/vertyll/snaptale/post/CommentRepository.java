package com.vertyll.snaptale.post;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByPostIdOrderByCreatedAtAscIdAsc(long postId);

    @Query("""
            SELECT new com.vertyll.snaptale.post.PostCount(c.postId, COUNT(c))
            FROM CommentEntity c WHERE c.postId IN :postIds GROUP BY c.postId""")
    List<PostCount> countByPostIds(Collection<Long> postIds);
}
