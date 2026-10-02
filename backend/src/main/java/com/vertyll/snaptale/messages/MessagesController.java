package com.vertyll.snaptale.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.common.Language;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RestController
final class MessagesController {

    private static final Duration CACHE = Duration.ofHours(1);

    private final Map<Language, Map<String, String>> messages;

    MessagesController(ObjectMapper objectMapper) {
        this.messages = Arrays.stream(Language.values())
            .collect(Collectors.toUnmodifiableMap(Function.identity(), language -> load(objectMapper, language)));
    }

    static String resource(Language language) {
        return "messages/" + language.tag() + ".json";
    }

    @GetMapping("/api/messages")
    ResponseEntity<Map<String, String>> messages(@RequestParam(name = "lang", required = false) @Nullable String lang) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(CACHE).cachePublic())
            .body(Objects.requireNonNull(messages.get(Language.of(lang))));
    }

    private static Map<String, String> load(ObjectMapper objectMapper, Language language) {
        String resource = resource(language);
        try (InputStream input = new ClassPathResource(resource).getInputStream()) {
            return Map.copyOf(objectMapper.readValue(input, new TypeReference<Map<String, String>>() {
            }));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + resource, e);
        }
    }
}
