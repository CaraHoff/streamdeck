package de.carahoff.streamdeck.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtils {
    public static Image flipHorizontally(Image originalImage) {
        BufferedImage bufferedImage = toBufferedImage(originalImage);

        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-bufferedImage.getWidth(null), 0);

        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        bufferedImage = op.filter(bufferedImage, null);

        return bufferedImage;
    }

    public static Image flipVertically(Image originalImage) {
        BufferedImage bufferedImage = toBufferedImage(originalImage);

        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -bufferedImage.getHeight(null));

        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        bufferedImage = op.filter(bufferedImage, null);

        return bufferedImage;
    }

    public static Image flipHorizontallyAndVertically(Image originalImage) {
        BufferedImage bufferedImage = toBufferedImage(originalImage);

        AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
        tx.translate(-bufferedImage.getWidth(null), -bufferedImage.getHeight(null));

        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        bufferedImage = op.filter(bufferedImage, null);

        return bufferedImage;
    }

    public static byte[] convertToJPGByteArray(Image image) {
        BufferedImage bufferedImage = toBufferedImage(image);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage && ((BufferedImage) image).getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return (BufferedImage) image;
        }

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return bufferedImage;
    }

    public static BufferedImage loadImage(String imageName) throws IOException {
        // Assuming the resources folder is in the classpath
        ClassLoader classLoader = ImageUtils.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("resources/" + imageName)) {
            if (inputStream == null) {
                throw new IOException("Image not found: " + imageName);
            }
            return ImageIO.read(inputStream);
        }
    }
}
