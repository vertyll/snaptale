package com.vertyll.snaptale.media;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.vertyll.snaptale.common.InvalidRequestException;
import com.vertyll.snaptale.common.MessageKeys;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AvatarImages {

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpeg", "JPEG", "png", "PNG");

    private final MediaProperties properties;

    BufferedImage cropAndScale(MultipartFile file, AvatarCrop crop) {
        return scaled(crop(decode(file), crop));
    }

    private static BufferedImage decode(MultipartFile file) {
        try (InputStream input = file.getInputStream();
                ImageInputStream image = ImageIO.createImageInputStream(input)) {
            Iterator<ImageReader> readers =
                    image == null ? Collections.emptyIterator() : ImageIO.getImageReaders(image);
            if (!readers.hasNext()) {
                throw new InvalidRequestException(MessageKeys.MEDIA_IMAGE_UNSUPPORTED);
            }
            ImageReader reader = readers.next();
            try {
                if (!SUPPORTED_FORMATS.contains(reader.getFormatName())) {
                    throw new InvalidRequestException(MessageKeys.MEDIA_IMAGE_UNSUPPORTED);
                }
                reader.setInput(image);
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new InvalidRequestException(MessageKeys.MEDIA_IMAGE_UNSUPPORTED, e);
        }
    }

    private static BufferedImage crop(BufferedImage image, AvatarCrop crop) {
        boolean inside = crop.x() + crop.width() <= image.getWidth() && crop.y() + crop.height() <= image.getHeight();
        if (!inside) {
            throw new InvalidRequestException(MessageKeys.MEDIA_CROP_INVALID);
        }
        return image.getSubimage(crop.x(), crop.y(), crop.width(), crop.height());
    }

    private BufferedImage scaled(BufferedImage image) {
        int longestSide = Math.max(image.getWidth(), image.getHeight());
        double ratio = Math.min(1.0, (double) properties.avatarSize() / longestSide);
        int width = Math.max(1, (int) Math.round(image.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(image.getHeight() * ratio));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }
}
