package com.vertyll.snaptale.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.security.Viewer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
class UserController {

    private static final int SUGGESTED_USERS = 5;

    private final UserDirectory directory;

    @GetMapping("/suggested")
    List<UserSummary> suggested(Viewer viewer) {
        return directory.suggested(viewer, SUGGESTED_USERS);
    }
}
