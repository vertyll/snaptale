package com.vertyll.snaptale.media;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import com.vertyll.snaptale.TestMedia;
import com.vertyll.snaptale.common.InvalidRequestException;
import com.vertyll.snaptale.common.MessageKeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaStorageTest {

    @TempDir
    Path directory;

    @Test
    void storesVideosUnderADatedPathAndDeletesThem() {
        MediaStorage storage = storage(DataSize.ofMegabytes(1));

        String path = storage.storeVideo(TestMedia.video("video"));

        assertThat(path).matches("videos/2026/09/[0-9a-f-]{36}\\.mp4");
        assertThat(directory.resolve(path)).exists();
        storage.deleteAfterCommit(path);
        assertThat(directory.resolve(path)).doesNotExist();
    }

    @Test
    void rejectsOversizedVideos() {
        MediaStorage storage = storage(DataSize.ofBytes(4));

        MockMultipartFile video = TestMedia.video("video");

        assertThatThrownBy(() -> storage.storeVideo(video)).isInstanceOf(InvalidRequestException.class)
            .hasMessage(MessageKeys.MEDIA_VIDEO_TOO_LARGE);
    }

    @Test
    void avatarsAreCroppedAndScaledDown() throws IOException {
        MediaStorage storage = storage(DataSize.ofMegabytes(1));

        String path = storage.storeAvatar(TestMedia.png("image", 2000, 1000), new AvatarCrop(0, 0, 1000, 1000));

        assertThat(javax.imageio.ImageIO.read(directory.resolve(path).toFile()).getWidth()).isEqualTo(512);
    }

    @Test
    void publicUrlsLiveUnderMedia() {
        assertThat(MediaStorage.publicUrl("videos/a.mp4")).isEqualTo("/media/videos/a.mp4");
    }

    private MediaStorage storage(DataSize maxVideo) {
        MediaProperties properties = new MediaProperties(directory, maxVideo, DataSize.ofMegabytes(5), 512);
        return new MediaStorage(
            properties,
            new AvatarImages(properties),
            Clock.fixed(Instant.parse("2026-09-26T10:00:00Z"), ZoneOffset.UTC)
        );
    }
}
