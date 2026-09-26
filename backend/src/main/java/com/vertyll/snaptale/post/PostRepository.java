package com.vertyll.snaptale.post;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface PostRepository extends JpaRepository<PostEntity, Long> {

    Slice<PostEntity> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    List<PostEntity> findByAuthorIdOrderByCreatedAtDescIdDesc(long authorId);

    @Query("SELECT p.id FROM PostEntity p WHERE p.authorId = :authorId ORDER BY p.createdAt DESC, p.id DESC")
    List<Long> findIdsByAuthorId(long authorId);
}
