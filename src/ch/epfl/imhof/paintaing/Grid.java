package ch.epfl.imhof.paintaing;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;

import static java.awt.Font.PLAIN;
import static java.lang.Math.round;

public final class Grid {
    private final static int LEN = 1;
    private final static int OFFSET = 0;
    private final static int STROKE = 2;
    private final static int GRID_SIZE = 8;
    private static String font_name = "Helvetica Neue";
    private static Color border_color = Color.WHITE;
    private static  int stroke = 1;

    private Grid() {}

    public static BufferedImage drawGrid(BufferedImage img) {
        return drawGrid(img, font_name, border_color, stroke);
    }

    public static BufferedImage drawGrid(BufferedImage img, String font_name) {
        return drawGrid(img, font_name, border_color, stroke);
    }

    public static BufferedImage drawGrid(BufferedImage img, Color border_color) {
        return drawGrid(img, font_name, border_color, stroke);
    }

    public static BufferedImage drawGrid(BufferedImage img, String font_name, Color border_color, int stroke) {
        int borderSize = img.getWidth() / 50;
        int newWidth = img.getWidth() + borderSize * 2;
        int newHeight = img.getHeight() + borderSize * 2;

        int step = (int) round(img.getWidth() / 8d);
        int fontSize = step / 10;
        int topMargin = (borderSize + fontSize) / 2;
        int leftMargin = (fontSize) / 2;


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

        Graphics2D withGrid = withBorder.createGraphics();

        withGrid.setColor(java.awt.Color.DARK_GRAY);
        withGrid.setFont(new Font(font_name, PLAIN, fontSize));
        withGrid.setStroke(new BasicStroke(STROKE));

        for (int i = 0; i < GRID_SIZE; i++) {
            int p = (i + 1) * step + borderSize;
            int cp = p - (step + fontSize)/ 2;
            if (p < newWidth) {
                withGrid.drawChars(new char[]{(char) ('A' + i)}, OFFSET, LEN, cp, topMargin);
                withGrid.drawRect(p, 0, stroke, newHeight);
            }
            if (p < newHeight) {
                withGrid.drawChars(new char[]{(char) ('0' + i)}, OFFSET, LEN, leftMargin, cp + fontSize/ 2);
                withGrid.drawRect(0, p, newWidth, stroke);
            }
        }
//        if (newWidth - 7 * step - step / 2 > 0)
//            withGrid.drawChars(new char[]{'H'}, OFFSET, LEN, 7 * step + step / 2, topMargin);

        withGrid.drawImage(withBorder, 0, 0, null);
        return withBorder;
    }

    private static boolean isBorder(int i, int j, int width, int height, int borderSize) {
        return (j <= borderSize || height - borderSize <= j)
                || (i <= borderSize || width - borderSize <= i);
    }
}
