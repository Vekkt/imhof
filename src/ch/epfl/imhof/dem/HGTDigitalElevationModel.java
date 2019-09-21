package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Vector3;
import static ch.epfl.imhof.Preconditions.checkArgument;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

public final class HGTDigitalElevationModel implements DigitalElevationModel {
    private final static double ANGULAR_RESOLUTION = toRadians(1 / 3600d);
    private final static double SAMPLE_DISTANCE = Earth.RADIUS * ANGULAR_RESOLUTION;
    private final static int SAMPLES_PER_DEGREE = 3600;
    private final static int FILE_SIZE = 25_934_402;
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

        int lo0 = (int) ((p.longitude() - lon) / ANGULAR_RESOLUTION);
        int la0 = (int) ((p.latitude() - lat) / ANGULAR_RESOLUTION);

        double z00 = buffer.get(flatIndex(lo0, la0));
        double z10 = buffer.get(flatIndex(lo0 + 1, la0));
        double z01 = buffer.get(flatIndex(lo0, la0 + 1));
        double z11 = buffer.get(flatIndex(lo0 + 1, la0 + 1));

        double dza = z10 - z00;
        double dzb = z01 - z00;
        double dzc = z01 - z11;
        double dzd = z10 - z11;

        double u = 0.5 * SAMPLE_DISTANCE * (dzc - dza);
        double v = 0.5 * SAMPLE_DISTANCE * (dzd - dzb);
        double w = SAMPLE_DISTANCE * SAMPLE_DISTANCE;

        return new Vector3(u, v, w).normalized();
    }

    private int flatIndex(int i, int j) {
        return i + SAMPLES_PER_DEGREE * j;
    }

    private double index(double angle) {
        return angle * SAMPLES_PER_DEGREE;
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
