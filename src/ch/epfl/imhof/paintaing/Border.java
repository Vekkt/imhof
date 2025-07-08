package ch.epfl.imhof.paintaing;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static java.lang.Math.round;

public final class Border {
    private static final Color border_color = Color.WHITE;

    private Border() {}

    public static BufferedImage drawBorder(BufferedImage img) {
        int borderSize = img.getWidth() / 50;
        int newWidth = img.getWidth() + borderSize * 2;
        int newHeight = img.getHeight() + borderSize * 2;

        BufferedImage withBorder = new BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                if (isBorder(i, j, newWidth, newHeight, borderSize)) {
                    withBorder.setRGB(i, j, border_color.getRGB());
                } else {
                    withBorder.setRGB(i, j, img.getRGB(i - borderSize, j - borderSize));
                }
            }
        }
        return withBorder;
    }

    private static boolean isBorder(int i, int j, int width, int height, int borderSize) {
        return (j <= borderSize || height - borderSize <= j)
                || (i <= borderSize || width - borderSize <= i);
    }
}
