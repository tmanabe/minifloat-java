package io.github.tmanabe;

import java.util.List;
import java.util.Set;

public class INT8Dataset {
    private static float dotProduct(float[] floats, byte[] bytes) {
        assert floats.length == bytes.length;
        float result = 0f;
        for (int i = 0; i < floats.length; ++i) {
            result += floats[i] * INT8.decode(bytes[i]);
        }
        return result;
    }

    private final byte[][] byteMatrix;

    public INT8Dataset(FP32Dataset fp32Dataset) {
        byteMatrix = new byte[fp32Dataset.floatMatrix.length][fp32Dataset.shape.get(1)];
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            for (int j = 0; j < fp32Dataset.shape.get(1); ++j) {
                byteMatrix[i][j] = INT8.encode(fp32Dataset.floatMatrix[i][j]);
            }
        }
    }

    public float matMul(FP32Dataset fp32Dataset) {
        float result = 0f;
        for (float[] floats : fp32Dataset.floatMatrix) {
            for (byte[] bytes : byteMatrix) {
                result += dotProduct(floats, bytes);
            }
        }
        return result;
    }

    public float top(int capacity, FP32Dataset fp32Dataset, List<Set<Integer>> expects) {
        float total = 0f;
        Collector collector = new Collector(capacity);
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            for (int id = 0; id < byteMatrix.length; ++id) {
                collector.collect(dotProduct(fp32Dataset.floatMatrix[i], byteMatrix[id]), id);
            }
            Set<Integer> actual = collector.top(), expect = expects.get(i);
            actual.retainAll(expect);
            total += 1f * actual.size() / expect.size();
        }
        return total / fp32Dataset.floatMatrix.length;
    }
}
