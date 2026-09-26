package com.vertyll.snaptale.post;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vertyll.snaptale.common.ForbiddenException;
import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.NotFoundException;
import com.vertyll.snaptale.media.MediaStorage;
import com.vertyll.snaptale.security.Viewer;
import com.vertyll.snaptale.user.UserDirectory;
import com.vertyll.snaptale.user.UserSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class PostService {

    static final int FEED_PAGE_SIZE = 10;

    private final PostRepository posts;
    private final CommentRepository comments;
    private final LikeRepository likes;
    private final PostCards cards;
    private final UserDirectory users;
    private final MediaStorage media;
    private final Clock clock;

    @Transactional(readOnly = true)
    FeedPage feed(int page, Viewer viewer) {
        Slice<PostEntity> slice = posts.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(page, FEED_PAGE_SIZE));
        return new FeedPage(cards.of(slice.getContent(), viewer), slice.hasNext());
    }

    @Transactional(readOnly = true)
    PostDetails details(long postId, Viewer viewer) {
        PostEntity post = get(postId);
        List<CommentEntity> postComments = comments.findByPostIdOrderByCreatedAtAscIdAsc(postId);
        Map<Long, UserSummary> authors =
                users.summaries(postComments.stream().map(CommentEntity::getAuthorId).distinct().toList());
        List<CommentView> commentViews = postComments.stream()
            .map(
                comment -> new CommentView(
                    comment.requireId(),
                    comment.getText(),
                    comment.getCreatedAt(),
                    Objects.requireNonNull(authors.get(comment.getAuthorId()))
                )
            )
            .toList();
        return new PostDetails(cards.of(post, viewer), commentViews, posts.findIdsByAuthorId(post.getAuthorId()));
    }

    @Transactional
    PostCard create(long authorId, NewPostForm form, @Nullable MultipartFile video) {
        String videoPath = media.storeVideo(video);
        PostEntity post = posts.save(new PostEntity(authorId, form.text(), videoPath, clock.instant()));
        return cards.of(post, Viewer.of(authorId));
    }

    @Transactional
    void delete(long postId, long userId) {
        PostEntity post = get(postId);
        if (post.getAuthorId() != userId) {
            throw new ForbiddenException(MessageKeys.POST_NOT_OWNER);
        }
        posts.delete(post);
        media.deleteAfterCommit(post.getVideoPath());
    }

    @Transactional
    void like(long postId, long userId) {
        requireExists(postId);
        LikeId id = new LikeId(postId, userId);
        if (!likes.existsById(id)) {
            likes.save(new LikeEntity(id, clock.instant()));
        }
    }

    @Transactional
    void unlike(long postId, long userId) {
        likes.deleteById(new LikeId(postId, userId));
    }

    @Transactional
    CommentView comment(long postId, long userId, CommentRequest request) {
        requireExists(postId);
        CommentEntity comment = comments.save(new CommentEntity(postId, userId, request.text(), clock.instant()));
        UserSummary author = Objects.requireNonNull(users.summaries(List.of(userId)).get(userId));
        return new CommentView(comment.requireId(), comment.getText(), comment.getCreatedAt(), author);
    }

    @Transactional
    void deleteComment(long postId, long commentId, long userId) {
        CommentEntity comment = comments.findById(commentId)
            .filter(found -> found.getPostId() == postId)
            .orElseThrow(() -> new NotFoundException(MessageKeys.COMMENT_NOT_FOUND));
        if (comment.getAuthorId() != userId && get(postId).getAuthorId() != userId) {
            throw new ForbiddenException(MessageKeys.COMMENT_NOT_OWNER);
        }
        comments.delete(comment);
    }

    private PostEntity get(long postId) {
        return posts.findById(postId).orElseThrow(() -> new NotFoundException(MessageKeys.POST_NOT_FOUND));
    }

    private void requireExists(long postId) {
        if (!posts.existsById(postId)) {
            throw new NotFoundException(MessageKeys.POST_NOT_FOUND);
        }
    }
}
