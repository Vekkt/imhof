package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HGTDigitalElevationModelTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void westernLongitudeIsParsedFromFilenameHemisphere() throws IOException {
        Path tile = hgtTile("N46W007.hgt", 10, 20, 30, 40);

        try (HGTDigitalElevationModel dem = new HGTDigitalElevationModel(tile.toFile())) {
            short elevation = dem.bufferAt(point(-6.5, 46.5));

            assertEquals(30, elevation);
        }
    }

    @Test
    void normalAtAcceptsClosedUpperTileBoundary() throws IOException {
        Path tile = hgtTile("N46E006.hgt", 10, 20, 30, 40);

        try (HGTDigitalElevationModel dem = new HGTDigitalElevationModel(tile.toFile())) {
            assertDoesNotThrow(() -> dem.normalAt(point(7.0, 47.0)));
        }
    }

    private Path hgtTile(String name, int... elevations) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(elevations.length * Short.BYTES)
                .order(ByteOrder.BIG_ENDIAN);
        for (int elevation : elevations) {
            buffer.putShort((short) elevation);
        }

        Path tile = temporaryDirectory.resolve(name);
        Files.write(tile, buffer.array());
        return tile;
    }

    private static PointGeo point(double longitude, double latitude) {
        return new PointGeo(Math.toRadians(longitude), Math.toRadians(latitude));
    }
}
