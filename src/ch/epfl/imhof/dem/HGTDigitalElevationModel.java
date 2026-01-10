package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Preconditions;
import ch.epfl.imhof.Vector3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel.MapMode;

public final class HGTDigitalElevationModel implements DigitalElevationModel {

    private ShortBuffer buffer;
    private final FileInputStream stream;
    private final double s;
    private final int latitude;
    private final int longitude;
    private final int sideLength;

    public HGTDigitalElevationModel(File file) throws IOException, IllegalArgumentException {
        String fileName = file.getName();

        Preconditions.checkArgument(fileName.matches("[NS]\\d{2}[EW]\\d{3}\\.hgt"), "Invalid file name.");

        long length = file.length();
        Preconditions.checkArgument(((Math.sqrt(length / 2d) - 1)) % 1 == 0,"Invalid file size.");
        sideLength = (int) (Math.sqrt(length / 2d) - 1);

        longitude = ((fileName.charAt(0) == 'S') ? -1 : 1) * Integer.parseInt(fileName.substring(4, 7));
        latitude = ((fileName.charAt(0) == 'S') ? -1 : 1) * Integer.parseInt(fileName.substring(1, 3));

        Preconditions.inCloseBounds(-180, longitude, 180);
        Preconditions.inCloseBounds(-90, latitude, 90);

        stream = new FileInputStream(file);
        s = Earth.RADIUS * Math.toRadians(1. / sideLength);
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

        double dx = preciseCoords.preciseI() - i;
        double dy = preciseCoords.preciseJ() - j;

        double z00 = bufferAt(i, j);
        double z10 = bufferAt(i + 1, j);
        double z01 = bufferAt(i, j + 1);
        double z11 = bufferAt(i + 1, j + 1);

        double dzdx = ((1 - dy) * (z10 - z00) + dy * (z11 - z01)) / s;
        double dzdy = ((1 - dx) * (z01 - z00) + dx * (z11 - z10)) / s;

        return new Vector3(-dzdx, -dzdy, 1).normalized();
    }

    private FileCoords getGetFileCoords(PointGeo p) {
        double pointLatitude = Math.toDegrees(p.latitude());
        double pointLongitude = Math.toDegrees(p.longitude());
        Preconditions.inCloseBounds(latitude, pointLatitude, latitude + 1);
        Preconditions.inCloseBounds(longitude, pointLongitude, longitude + 1);

        double preciseI = (pointLongitude - longitude) * sideLength;
        double preciseJ = (pointLatitude - latitude) * sideLength;
        return new FileCoords(preciseI, preciseJ);
    }

    private record FileCoords(double preciseI, double preciseJ) {}

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