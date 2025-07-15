package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Preconditions;
import ch.epfl.imhof.Vector3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;

public final class HGTDigitalElevationModel implements DigitalElevationModel {

    private ShortBuffer buffer;
    private final FileInputStream stream;
    private final Map<Integer, Vector3> normalMap;
    private final double s;
    private final double s_2;
    private final int latitude;
    private final int longitude;
    private final int sideLength;

    public HGTDigitalElevationModel(File file) throws IOException, IllegalArgumentException {
        String fileName = file.getName();

        Preconditions.checkArgument(!fileName.matches("[NS]\\d{2}[EW]\\d{3}\\.hgt"), "Invalid file name.");

        long length = file.length();
        Preconditions.checkArgument(((Math.sqrt(length / 2d) - 1)) % 1 != 0,"Invalid file size.");
        sideLength = (int) (Math.sqrt(length / 2d) - 1);

        longitude = ((fileName.charAt(0) == 'S') ? -1 : 1) * Integer.parseInt(fileName.substring(4, 7));
        latitude = ((fileName.charAt(0) == 'S') ? -1 : 1) * Integer.parseInt(fileName.substring(1, 3));

        Preconditions.inCloseBounds(-180, longitude, 180);
        Preconditions.inCloseBounds(-90, latitude, 90);

        normalMap = new HashMap<>();
        stream = new FileInputStream(file);
        s = Earth.RADIUS * Math.toRadians(1. / sideLength);
        s_2 = s * s * 8;
        buffer = stream.getChannel().map(MapMode.READ_ONLY, 0, length).asShortBuffer();
    }

    @Override
    public void close() throws IOException {
        stream.close();
        buffer = null;
    }

    @Override
    public Vector3 normalAt(PointGeo p) throws IllegalArgumentException {
        FileCoords preciseCoords = getGetFileCoords(p);

        int i = (int) preciseCoords.preciseI();
        int j = (int) preciseCoords.preciseJ();

        Vector3 topLeft = getVertexNormal(i, j + 1);
        Vector3 topRight = getVertexNormal(i + 1, j + 1);
        Vector3 bottomLeft = getVertexNormal(i, j);
        Vector3 bottomRight = getVertexNormal(i + 1, j);

        return interpolatedVector(bottomLeft, bottomRight, topLeft, topRight,
                preciseCoords.preciseI() - i, preciseCoords.preciseJ() - j);
    }

    private FileCoords getGetFileCoords(PointGeo p) {
        double pointLatitude = Math.toDegrees(p.latitude());
        double pointLongitude = Math.toDegrees(p.longitude());

        Preconditions.inCloseBounds(pointLatitude, latitude, pointLatitude-1);
        Preconditions.inCloseBounds(pointLongitude, longitude, pointLongitude - 1);

        double preciseI = (pointLongitude - longitude) * sideLength;
        double preciseJ = (pointLatitude - latitude) * sideLength;
        return new FileCoords(preciseI, preciseJ);
    }

    private record FileCoords(double preciseI, double preciseJ) {}

    private Vector3 getVertexNormal(int i, int j) {
        double h1 = bufferAt(i + 1, j) * 2;
        double h2 = bufferAt(i + 1, j + 1);
        double h3 = bufferAt(i, j + 1) * 2;
        double h4 = bufferAt(i - 1, j + 1);
        double h5 = bufferAt(i - 1, j) * 2;
        double h6 = bufferAt(i - 1, j - 1);
        double h7 = bufferAt(i, j - 1) * 2;
        double h8 = bufferAt(i + 1, j - 1);

        return normalMap.computeIfAbsent(indexOf(i, j), k ->
                new Vector3(s * (h5 + h6 + h4 - h1 - h2 - h8), s * (h7 + h6 + h8 - h3 - h2 - h4), s_2).normalized());
    }

    private Vector3 interpolatedVector(Vector3 bl, Vector3 br, Vector3 tl, Vector3 tr, double dx, double dy) {
        return new Vector3(
                bilinearInterpolation(bl.x(), br.x(), tl.x(), tr.x(), dx, dy),
                bilinearInterpolation(bl.y(), br.y(), tl.y(), tr.y(), dx, dy),
                bilinearInterpolation(bl.z(), br.z(), tl.z(), tr.z(), dx, dy));
    }

    private double bilinearInterpolation(double bl, double br, double tl, double tr, double dx, double dy) {
        double r1 = bl * (1 - dx) + br * dx;
        double r2 = tl * (1 - dx) + tr * dx;
        return r1 * (1 - dy) + r2 * dy;
    }

    private short bufferAt(int i, int j) {
        return buffer.get(indexOf(i, j));
    }

    public short bufferAt(PointGeo p) {
        FileCoords preciseCoords = getGetFileCoords(p);
        return bufferAt((int) preciseCoords.preciseI(), (int) preciseCoords.preciseJ());
    }
    
    private int indexOf(int i, int j) {
        return (sideLength - j) * (sideLength + 1) + i;
    }
}