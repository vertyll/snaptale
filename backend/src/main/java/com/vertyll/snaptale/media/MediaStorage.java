package com.vertyll.snaptale.media;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.vertyll.snaptale.common.InvalidRequestException;
import com.vertyll.snaptale.common.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStorage {

    static final String PUBLIC_PREFIX = "/media/";

    private static final byte[] MP4_BOX_TYPE = {
        'f',
        't',
        'y',
        'p'
    };
    private static final int MP4_BOX_TYPE_OFFSET = 4;
    private static final String MAX_MEGABYTES = "maxMegabytes";

    private final MediaProperties properties;
    private final AvatarImages avatarImages;
    private final Clock clock;

    public String storeVideo(@Nullable MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException(MessageKeys.MEDIA_VIDEO_REQUIRED);
        }
        if (file.getSize() > properties.maxVideoSize().toBytes()) {
            throw new InvalidRequestException(
                MessageKeys.MEDIA_VIDEO_TOO_LARGE,
                Map.of(MAX_MEGABYTES, properties.maxVideoSize().toMegabytes())
            );
        }
        if (!isMp4(file)) {
            throw new InvalidRequestException(MessageKeys.MEDIA_VIDEO_UNSUPPORTED);
        }
        String path = newPath("videos", "mp4");
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, resolve(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store the uploaded video", e);
        }
        discardOnRollback(path);
        return path;
    }

    public String storeAvatar(@Nullable MultipartFile file, AvatarCrop crop) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException(MessageKeys.MEDIA_IMAGE_REQUIRED);
        }
        if (file.getSize() > properties.maxImageSize().toBytes()) {
            throw new InvalidRequestException(
                MessageKeys.MEDIA_IMAGE_TOO_LARGE,
                Map.of(MAX_MEGABYTES, properties.maxImageSize().toMegabytes())
            );
        }
        BufferedImage avatar = avatarImages.cropAndScale(file, crop);
        String path = newPath("avatars", "jpg");
        try {
            ImageIO.write(avatar, "jpg", resolve(path).toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store the avatar", e);
        }
        discardOnRollback(path);
        return path;
    }

    public void deleteAfterCommit(@Nullable String path) {
        if (path == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delete(path);
                }
            });
        } else {
            delete(path);
        }
    }

    private void discardOnRollback(String path) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        delete(path);
                    }
                }
            });
        }
    }

    private void delete(String path) {
        try {
            Files.deleteIfExists(resolve(path));
        } catch (IOException e) {
            log.warn("[WARN] Failed to delete media file {}", path, e);
        }
    }

    public static @Nullable String publicUrl(@Nullable String path) {
        return path == null ? null : PUBLIC_PREFIX + path;
    }

    private String newPath(String kind, String extension) {
        LocalDate today = LocalDate.now(clock);
        String path = "%s/%d/%02d/%s.%s"
            .formatted(kind, today.getYear(), today.getMonthValue(), UUID.randomUUID(), extension);
        try {
            Files.createDirectories(Objects.requireNonNull(resolve(path).getParent()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create the media directory for " + path, e);
        }
        return path;
    }

    private Path resolve(String path) {
        Path root = properties.directory().toAbsolutePath().normalize();
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Media path escapes the media directory: " + path);
        }
        return resolved;
    }

    private static boolean isMp4(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(MP4_BOX_TYPE_OFFSET + MP4_BOX_TYPE.length);
            return header.length == MP4_BOX_TYPE_OFFSET + MP4_BOX_TYPE.length
                    && Arrays.equals(Arrays.copyOfRange(header, MP4_BOX_TYPE_OFFSET, header.length), MP4_BOX_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the uploaded video", e);
        }
    }

}
