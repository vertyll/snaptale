package com.vertyll.snaptale;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.mock.web.MockMultipartFile;

public final class TestMedia {

    private static final byte[] MP4_HEADER = {
        0,
        0,
        0,
        0x18,
        'f',
        't',
        'y',
        'p',
        'i',
        's',
        'o',
        'm',
        0,
        0,
        2,
        0
    };

    private TestMedia() {
    }

    public static MockMultipartFile video(String part) {
        return new MockMultipartFile(part, "clip.mp4", "video/mp4", MP4_HEADER);
    }

    public static MockMultipartFile notAVideo(String part) {
        return new MockMultipartFile(
            part,
            "clip.mp4",
            "video/mp4",
            "not a video".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    public static MockMultipartFile png(String part, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile(part, "avatar.png", "image/png", output.toByteArray());
    }
}
