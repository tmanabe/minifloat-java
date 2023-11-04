package io.github.tmanabe;

import java.util.List;
import java.util.Set;

public class BFloat16Dataset {
    private static float dotProduct(float[] floats, short[] shorts) {
        assert floats.length == shorts.length;
        float result = 0f;
        for (int i = 0; i < floats.length; ++i) {
            result += floats[i] * BFloat16.decode(shorts[i]);
        }
        return result;
    }

    private final short[][] shortMatrix;

    public BFloat16Dataset(FP32Dataset fp32Dataset) {
        shortMatrix = new short[fp32Dataset.floatMatrix.length][fp32Dataset.shape.get(1)];
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            for (int j = 0; j < fp32Dataset.shape.get(1); ++j) {
                shortMatrix[i][j] = BFloat16.encode(fp32Dataset.floatMatrix[i][j]);
            }
        }
    }

    public float matMul(FP32Dataset fp32Dataset) {
        float result = 0f;
        for (float[] floats : fp32Dataset.floatMatrix) {
            for (short[] shorts : shortMatrix) {
                result += dotProduct(floats, shorts);
            }
        }
        return result;
    }

    public float top(int capacity, FP32Dataset fp32Dataset, List<Set<Integer>> expects) {
        float total = 0f;
        Collector collector = new Collector(capacity);
        for (int i = 0; i < fp32Dataset.floatMatrix.length; ++i) {
            for (int id = 0; id < shortMatrix.length; ++id) {
                collector.collect(dotProduct(fp32Dataset.floatMatrix[i], shortMatrix[id]), id);
            }
            Set<Integer> actual = collector.top(), expect = expects.get(i);
            actual.retainAll(expect);
            total += 1f * actual.size() / expect.size();
        }
        return total / fp32Dataset.floatMatrix.length;
    }
}
