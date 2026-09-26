package com.vertyll.snaptale.profile;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.follow.FollowGraph;
import com.vertyll.snaptale.post.PostCatalog;
import com.vertyll.snaptale.security.Viewer;
import com.vertyll.snaptale.user.UserDirectory;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
class ProfileController {

    private final UserDirectory users;
    private final FollowGraph follows;
    private final PostCatalog posts;

    @GetMapping("/api/users/{userId}")
    @Transactional(readOnly = true)
    ProfileResponse profile(Viewer viewer, @PathVariable long userId) {
        return new ProfileResponse(
            users.profile(userId),
            follows.counts(userId),
            follows.follows(viewer, userId),
            posts.byAuthor(userId, viewer)
        );
    }
}
