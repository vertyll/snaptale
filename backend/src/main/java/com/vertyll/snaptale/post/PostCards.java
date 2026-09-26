package com.vertyll.snaptale.post;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vertyll.snaptale.media.MediaStorage;
import com.vertyll.snaptale.security.Viewer;
import com.vertyll.snaptale.user.UserDirectory;
import com.vertyll.snaptale.user.UserSummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PostCards {

    private final UserDirectory users;
    private final LikeRepository likes;
    private final CommentRepository comments;

    List<PostCard> of(List<PostEntity> posts, Viewer viewer) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> ids = posts.stream().map(PostEntity::requireId).toList();
        Map<Long, UserSummary> authors =
                users.summaries(posts.stream().map(PostEntity::getAuthorId).distinct().toList());
        Map<Long, Long> likeCounts = asMap(likes.countByPostIds(ids));
        Map<Long, Long> commentCounts = asMap(comments.countByPostIds(ids));
        Set<Long> liked =
                viewer.userId().map(userId -> Set.copyOf(likes.findLikedPostIds(userId, ids))).orElse(Set.of());
        return posts.stream()
            .map(
                post -> new PostCard(
                    post.requireId(),
                    post.getText(),
                    Objects.requireNonNull(MediaStorage.publicUrl(post.getVideoPath())),
                    post.getCreatedAt(),
                    Objects.requireNonNull(authors.get(post.getAuthorId())),
                    likeCounts.getOrDefault(post.requireId(), 0L),
                    commentCounts.getOrDefault(post.requireId(), 0L),
                    liked.contains(post.requireId())
                )
            )
            .toList();
    }

    PostCard of(PostEntity post, Viewer viewer) {
        return of(List.of(post), viewer).getFirst();
    }

    private static Map<Long, Long> asMap(Collection<PostCount> counts) {
        return counts.stream().collect(Collectors.toUnmodifiableMap(PostCount::postId, PostCount::count));
    }
}
