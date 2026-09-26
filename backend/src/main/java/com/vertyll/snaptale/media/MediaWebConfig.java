package com.vertyll.snaptale.media;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class MediaWebConfig implements WebMvcConfigurer {

    private static final Duration FILE_CACHE = Duration.ofDays(365);

    private final MediaProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path directory = properties.directory().toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create the media directory " + directory, e);
        }
        registry.addResourceHandler(MediaStorage.PUBLIC_PREFIX + "**")
            .addResourceLocations(directory.toUri().toString())
            .setCacheControl(CacheControl.maxAge(FILE_CACHE).cachePublic().immutable());
    }
}
