package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Vector3;
import static ch.epfl.imhof.Preconditions.checkArgument;
import static java.lang.Math.toRadians;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

public final class HGTDigitalElevationModel implements DigitalElevationModel {
    private final static int    FILE_SIZE          = 25_934_402;
    private final static int    SAMPLES_PER_DEGREE = 3600;
    private final static double ANGULAR_RESOLUTION = toRadians(1. / SAMPLES_PER_DEGREE);
    private final static double SAMPLE_DISTANCE    = Earth.RADIUS * ANGULAR_RESOLUTION;

    private int longitude, latitude;
    private ShortBuffer buffer;

    public HGTDigitalElevationModel(File file) throws Exception {
        checkArgument(file.length() == FILE_SIZE, "invalid file size");
        checkArgument(isNameValid(file.getName()), "invalid file name");

        longitude = file.getName().charAt(0) == 'S' ? (-1)*longitude : longitude;
        latitude = file.getName().charAt(3) == 'S' ? (-1)*latitude : latitude;

        try (FileInputStream s = new FileInputStream(file)) {
            buffer = s.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                    .asShortBuffer();
        }
    }

    @Override
    public Vector3 normalAt(PointGeo p) throws IllegalArgumentException {
        double lat = Math.toRadians(latitude);
        double lon = Math.toRadians(longitude);

        double iprec = ((p.longitude() - lon) / ANGULAR_RESOLUTION);
        double jprec = ((p.latitude() - lat) / ANGULAR_RESOLUTION);
        int i = (int) iprec;
        int j = (int) jprec;

        double z00 = buffer.get(flatIndex(i, j));
        double z10 = buffer.get(flatIndex(i + 1, j));
        double z01 = buffer.get(flatIndex(i, j + 1));
        double z11 = buffer.get(flatIndex(i + 1, j + 1));

        Vector3 a = new Vector3(SAMPLE_DISTANCE, 0, z10 - z00);
        Vector3 b = new Vector3(0, SAMPLE_DISTANCE, z01 - z00);
        Vector3 c = new Vector3(-SAMPLE_DISTANCE, 0, z01 - z11);
        Vector3 d = new Vector3(0, -SAMPLE_DISTANCE, z10 - z11);

        Vector3 n1 = a.prod(b);
        Vector3 n2 = c.prod(d);
        Vector3 n3 = d.prod(a);
        Vector3 n4 = b.prod(c);

        return interpolateVector(n1, n3, n2, n4, iprec-i, jprec-j).normalized();
    }

    private Vector3 interpolateVector(Vector3 bl, Vector3 br, Vector3 tr, Vector3 tl, double dx, double dy) {
        return new Vector3(
                bilinearInterpolation(bl.x(), br.x(), tr.x(), tl.x(), dx, dy),
                bilinearInterpolation(bl.y(), br.y(), tr.y(), tl.y(), dx, dy),
                bilinearInterpolation(bl.z(), br.z(), tr.z(), tl.z(), dx, dy)
        );
    }

    private double bilinearInterpolation(double bl, double br, double tr, double tl, double dx, double dy) {
        double deltafx = br - bl, deltafy = tl - bl;
        double deltafxy = bl + tr - br - tl;
        return deltafx * dx + deltafy * dy + deltafxy * dx * dy + bl;
    }

    private int flatIndex(int i, int j) {
        return i + SAMPLES_PER_DEGREE * j;
    }

    @Override
    public void close() {
        buffer.clear();
        buffer = null;
    }

    private boolean isNameValid(String name) {
        try {
            latitude = Integer.parseInt(name.substring(1, 3));
            longitude = Integer.parseInt(name.substring(4, 7));

            return (name.length() == 11)
                    && (name.charAt(0) == 'N' || name.charAt(0) == 'S')
                    && (0 <= longitude && longitude <= 180)
                    && (name.charAt(3) == 'E' || name.charAt(3) == 'W')
                    && (0 <= latitude && latitude <= 90)
                    && (name.substring(7).equals(".hgt"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }
}
