package com.vertyll.snaptale.post;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.snaptale.security.Viewer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCatalog {

    private final PostRepository posts;
    private final PostCards cards;

    public List<PostCard> byAuthor(long authorId, Viewer viewer) {
        return cards.of(posts.findByAuthorIdOrderByCreatedAtDescIdDesc(authorId), viewer);
    }
}
