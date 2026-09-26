package com.vertyll.snaptale.post;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vertyll.snaptale.security.CurrentUser;
import com.vertyll.snaptale.security.Viewer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
class PostController {

    private static final int MAX_PAGE = 10_000;

    private final PostService service;

    @GetMapping
    FeedPage feed(Viewer viewer, @RequestParam(defaultValue = "0") @PositiveOrZero @Max(MAX_PAGE) int page) {
        return service.feed(page, viewer);
    }

    @GetMapping("/{postId}")
    PostDetails details(Viewer viewer, @PathVariable long postId) {
        return service.details(postId, viewer);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    PostCard create(
        CurrentUser user,
        @Valid @ModelAttribute NewPostForm form,
        @RequestPart(name = "video", required = false) @Nullable MultipartFile video
    ) {
        return service.create(user.id(), form, video);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(CurrentUser user, @PathVariable long postId) {
        service.delete(postId, user.id());
    }

    @PutMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void like(CurrentUser user, @PathVariable long postId) {
        service.like(postId, user.id());
    }

    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlike(CurrentUser user, @PathVariable long postId) {
        service.unlike(postId, user.id());
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    CommentView comment(CurrentUser user, @PathVariable long postId, @Valid @RequestBody CommentRequest request) {
        return service.comment(postId, user.id(), request);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(CurrentUser user, @PathVariable long postId, @PathVariable long commentId) {
        service.deleteComment(postId, commentId, user.id());
    }
}
