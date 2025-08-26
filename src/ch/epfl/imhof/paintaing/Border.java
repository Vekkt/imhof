package ch.epfl.imhof.paintaing;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.round;

public final class Border {
    private static final Color OUTER_BORDER = Color.WHITE;
    private static final Color INNER_BORDER = Color.BLACK;
    private static final int TOP_PADDING = 5;

    private Border() {}

    public static BufferedImage drawBorder(BufferedImage img, String title) throws IOException, FontFormatException {
        int borderSize = img.getWidth() / 50;
        int newWidth = img.getWidth() + borderSize * 2;
        int newHeight = img.getHeight() + borderSize * (1 + TOP_PADDING);

        BufferedImage withBorder = new BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                if (isBorder(i, j, newWidth, newHeight, borderSize)) {
                    withBorder.setRGB(i, j, OUTER_BORDER.getRGB());
                } else if (isInnerBorder(i, j, newWidth, newHeight, borderSize)) {
                    withBorder.setRGB(i, j, INNER_BORDER.getRGB());
                } else {
                    withBorder.setRGB(i, j, img.getRGB(i - borderSize, j - TOP_PADDING * borderSize));
                }
            }
        }

        Graphics2D g2d = withBorder.createGraphics();

        String fontPath = "ressources/fonts/GeographicaHand_Regular.otf";

        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont(340f);

        g2d.setFont(customFont);
        g2d.setColor(Color.BLACK);
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(title,
                (newWidth - metrics.stringWidth(title)) / 2,
                (borderSize * TOP_PADDING + metrics.getHeight()) / 2 - borderSize + metrics.getDescent());

        return withBorder;
    }

    private static boolean isBorder(int i, int j, int width, int height, int borderSize) {
        return (j < TOP_PADDING * borderSize || height - borderSize < j)
                || (i < borderSize || width - borderSize < i);
    }

    private static boolean isInnerBorder(int i, int j, int width, int height, int borderSize) {
        return (j == TOP_PADDING * borderSize || height - borderSize == j)
                || (i == borderSize || width - borderSize == i);
    }
}
