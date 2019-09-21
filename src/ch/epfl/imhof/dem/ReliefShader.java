package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Vector3;
import ch.epfl.imhof.geometry.Point;
import ch.epfl.imhof.projection.Projection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.function.Function;

import static ch.epfl.imhof.geometry.Point.alignedCoordinateChange;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.awt.image.ConvolveOp.EDGE_NO_OP;
import static java.lang.Math.*;
import static java.util.Objects.requireNonNull;

public final class ReliefShader {
    private Projection proj;
    private DigitalElevationModel dem;
    private Vector3 light;

    public ReliefShader(Projection proj, DigitalElevationModel dem, Vector3 light) {
        this.proj = requireNonNull(proj);
        this.dem = requireNonNull(dem);
        this.light = requireNonNull(light.normalized());
    }

    public BufferedImage shadedRelief(Point bl, Point tr, int width, int height, float r) {
        Function<Point, Point> newRef = alignedCoordinateChange(new Point(0, height), bl, new Point(width, 0), tr);

        float[] kernelData = shadingKernel(r);
        Kernel hkernel = new Kernel(kernelData.length, 1, kernelData);
        Kernel vkernel = new Kernel(1, kernelData.length, kernelData);

        return applyShade(getRawRelief(width, height, newRef), hkernel, vkernel);
    }

    private BufferedImage applyShade(BufferedImage raw, Kernel hkernel, Kernel vkernel) {
        ConvolveOp hConvolution = new ConvolveOp(hkernel, EDGE_NO_OP, null);
        ConvolveOp vConvolution = new ConvolveOp(vkernel, EDGE_NO_OP, null);
        return vConvolution.filter(hConvolution.filter(raw, null), null);
    }

    private BufferedImage getRawRelief(int width, int height, Function<Point, Point> ref) {
        BufferedImage img = new BufferedImage(width, height, TYPE_INT_RGB);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                PointGeo p = proj.inverse(ref.apply(new Point(i, j)));
                Vector3 slope = dem.normalAt(p);

                float ctheta = (float) cos(light.scalarProduct(slope));
                float rg = 0.5f * (1f + ctheta);
                float b = 0.5f * (1 + 0.7f * ctheta);

                img.setRGB(i, j, new Color(rg, rg, b).getRGB());
            }
        }
        return img;
    }

    private float[] shadingKernel(float radius) {
        int n = 2 * (int) ceil(radius) + 1;
        int halfn = (int) floor(n / 2f);
        float sigma = (2f * (float) pow(radius * 3, 2));

        float[] kernel = new float[n];
        float weight = 0;

        for (int x = -halfn; x < halfn; x++) {
            float val = (float) exp(-(x * x) / sigma);
            kernel[x + halfn] = val;
            weight += val;
        }

        for (int x = 0; x < n; x++)
            kernel[x] /= weight;

        return kernel;
    }
}
