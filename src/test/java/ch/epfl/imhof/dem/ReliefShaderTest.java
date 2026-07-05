package ch.epfl.imhof.dem;

import ch.epfl.imhof.PointGeo;
import ch.epfl.imhof.Vector3;
import ch.epfl.imhof.projection.EquirectangularProjection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReliefShaderTest {
    @Test
    void shadingKernelIsSymmetricAndNormalized() throws Exception {
        ReliefShader shader = new ReliefShader(
                new EquirectangularProjection(),
                new FlatDigitalElevationModel(),
                new Vector3(0, 0, 1));

        Method method = ReliefShader.class.getDeclaredMethod("shadingKernel", float.class);
        method.setAccessible(true);

        float[] kernel = (float[]) method.invoke(shader, 1f);
        float sum = 0;
        for (float weight : kernel) {
            sum += weight;
        }

        assertEquals(3, kernel.length);
        assertTrue(kernel[0] > 0);
        assertTrue(kernel[2] > 0);
        assertEquals(kernel[0], kernel[2], 1e-6f);
        assertEquals(1f, sum, 1e-6f);
    }

    private static final class FlatDigitalElevationModel implements DigitalElevationModel {
        @Override
        public Vector3 normalAt(PointGeo p) {
            return new Vector3(0, 0, 1);
        }

        @Override
        public short bufferAt(PointGeo p) {
            return 0;
        }

        @Override
        public void close() {}
    }
}
