package io.github.tmanabe;

import java.util.List;
import java.util.Set;

public class INT4Dataset {
    private byte[][] byteMatrix;

    public INT4Dataset(FP32Dataset fp32Dataset) {
        assert 0 == fp32Dataset.shape.get(1) % 2;
        byteMatrix = new byte[fp32Dataset.floatMatrix.length][fp32Dataset.shape.get(1) / 2];
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            byteMatrix[i] = INT4.batchEncode(fp32Dataset.floatMatrix[i]);
        }
    }

    public float matMul(FP32Dataset fp32Dataset) {
        float result = 0f;
        for (float[] floats : fp32Dataset.floatMatrix) {
            for (byte[] bytes : byteMatrix) {
                result += INT4.dotProduct(floats, bytes);
            }
        }
        return result;
    }

    public float top(int capacity, FP32Dataset fp32Dataset, List<Set<Integer>> expects) {
        float total = 0f;
        Collector collector = new Collector(capacity);
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            for (int id = 0; id < byteMatrix.length; ++id) {
                collector.collect(INT4.dotProduct(fp32Dataset.floatMatrix[i], byteMatrix[id]), id);
            }
            Set<Integer> actual = collector.top(), expect = expects.get(i);
            actual.retainAll(expect);
            total += 1f * actual.size() / expect.size();
        }
        return total / fp32Dataset.floatMatrix.length;
    }
}
