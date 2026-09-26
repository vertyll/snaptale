package com.vertyll.snaptale.follow;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.security.CurrentUser;
import com.vertyll.snaptale.user.UserSummary;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
class FollowController {

    private final FollowGraph graph;

    @GetMapping("/api/me/following")
    List<UserSummary> following(CurrentUser user) {
        return graph.followedBy(user.id());
    }

    @PutMapping("/api/users/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void follow(CurrentUser user, @PathVariable long userId) {
        graph.follow(user.id(), userId);
    }

    @DeleteMapping("/api/users/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unfollow(CurrentUser user, @PathVariable long userId) {
        graph.unfollow(user.id(), userId);
    }
}
