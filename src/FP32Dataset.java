import java.nio.FloatBuffer;
import java.util.List;

public class FP32Dataset {
    static float dotProduct(float[] a, float[] b) {
        assert a.length == b.length;
        float result = 0f;
        for (int i = 0; i < a.length; ++i) {
            result += a[i] * b[i];
        }
        return result;
    }

    List<Integer> shape;
    float[][] floatMatrix;

    FP32Dataset(List<Integer> shape, int limit, FloatBuffer floatBuffer) {
        assert 2 == shape.size();
        assert limit <= shape.get(0);
        this.shape = shape;
        floatMatrix = new float[limit][shape.get(1)];
        for (int i = 0; i < limit; ++i) {
            floatBuffer.get(floatMatrix[i]);
        }
    }

    float matMul(FP32Dataset fp32Dataset) {
        float result = 0f;
        for (float[] a : fp32Dataset.floatMatrix) {
            for (float[] b : floatMatrix) {
                result += dotProduct(a, b);
            }
        }
        return result;
    }
}
