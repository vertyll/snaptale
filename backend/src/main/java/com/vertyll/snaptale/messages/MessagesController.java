package com.vertyll.snaptale.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RestController
final class MessagesController {

    static final String RESOURCE = "messages/pl.json";

    private static final Duration CACHE = Duration.ofHours(1);

    private final Map<String, String> messages;

    MessagesController(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            this.messages = Map.copyOf(objectMapper.readValue(input, new TypeReference<Map<String, String>>() {
            }));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, e);
        }
    }

    @GetMapping("/api/messages")
    ResponseEntity<Map<String, String>> messages() {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(CACHE).cachePublic()).body(messages);
    }
}
