import java.util.List;
import java.util.Set;

class INT8Dataset {
    static float dotProduct(float[] a, byte[] b) {
        assert a.length == b.length;
        float result = 0f;
        for (int i = 0; i < a.length; ++i) {
            result += a[i] * b[i];
        }
        return result;
    }

    byte[][] byteMatrix;

    INT8Dataset(FP32Dataset fp32Dataset) {
        byteMatrix = new byte[fp32Dataset.floatMatrix.length][fp32Dataset.shape.get(1)];
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            for (int j = 0; j < fp32Dataset.shape.get(1); ++j) {
                byteMatrix[i][j] = INT8.encode(fp32Dataset.floatMatrix[i][j]);
            }
        }
    }

    float matMul(FP32Dataset fp32Dataset) {
        float result = 0f;
        for (float[] a : fp32Dataset.floatMatrix) {
            for (byte[] b : byteMatrix) {
                result += dotProduct(a, b);
            }
        }
        return result;
    }

    float top(int capacity, FP32Dataset fp32Dataset, List<Set<Integer>> expects) {
        float total = 0f;
        Collector collector = new Collector(capacity);
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            Set<Integer> expect = expects.get(i);
            for (int id = 0; id < byteMatrix.length; ++id) {
                collector.collect(dotProduct(fp32Dataset.floatMatrix[i], byteMatrix[id]), id);
            }
            Set<Integer> actual = collector.top();
            actual.retainAll(expect);
            total += 1f * actual.size() / expect.size();
        }
        return total / fp32Dataset.floatMatrix.length;
    }
}
