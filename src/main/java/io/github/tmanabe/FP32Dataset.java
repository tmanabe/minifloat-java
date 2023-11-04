package io.github.tmanabe;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FP32Dataset {
    private static float dotProduct(float[] a, float[] b) {
        assert a.length == b.length;
        float result = 0f;
        for (int i = 0; i < a.length; ++i) {
            result += a[i] * b[i];
        }
        return result;
    }

    List<Integer> shape;
    float[][] floatMatrix;

    public FP32Dataset(List<Integer> shape, int limit, FloatBuffer floatBuffer) {
        assert 2 == shape.size();
        assert limit <= shape.get(0);
        this.shape = shape;
        floatMatrix = new float[limit][shape.get(1)];
        for (int i = 0; i < limit; ++i) {
            floatBuffer.get(floatMatrix[i]);
        }
    }

    public float matMul(FP32Dataset fp32Dataset) {
        float result = 0f;
        for (float[] a : fp32Dataset.floatMatrix) {
            for (float[] b : floatMatrix) {
                result += dotProduct(a, b);
            }
        }
        return result;
    }

    public List<Set<Integer>> top(int capacity, FP32Dataset fp32Dataset) {
        List<Set<Integer>> results = new ArrayList<>();
        Collector collector = new Collector(capacity);
        for (float[] a : fp32Dataset.floatMatrix) {
            for (int id = 0; id < floatMatrix.length; ++id) {
                collector.collect(dotProduct(a, floatMatrix[id]), id);
            }
            results.add(collector.top());
        }
        return results;
    }
}
