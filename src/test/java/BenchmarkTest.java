import io.github.tmanabe.BFloat16Dataset;
import io.github.tmanabe.FP32Dataset;
import io.github.tmanabe.INT4Dataset;
import io.github.tmanabe.INT8Dataset;
import io.github.tmanabe.Safetensors;
import org.junit.Test;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class BenchmarkTest {
    private static class Stopwatch implements Closeable {
        private final long begin;

        private Stopwatch() {
            begin = System.currentTimeMillis();
        }

        @Override
        public void close() {
            System.out.println(System.currentTimeMillis() - begin + "ms.");
        }
    }

    private static FP32Dataset prepare(Safetensors safetensors, String tensorName, int limit) {
        System.out.println("Preparing " + limit + " " + tensorName);
        List<Integer> shape = safetensors.getHeader().get(tensorName).getShape();
        return new FP32Dataset(shape, limit, safetensors.getFloatBuffer(tensorName));
    }

    @Test
    public void test() throws IOException {
        URL url = this.getClass().getResource("arbitrary-embeddings/jp_test.safetensors");
        if (null == url) return;

        File file = new File(url.getFile());
        Safetensors safetensors = Safetensors.load(file);
        FP32Dataset queries = prepare(safetensors, "queries", 1024);
        FP32Dataset fp32titles = prepare(safetensors, "product_titles", 4096);
        BFloat16Dataset bFloat16titles = new BFloat16Dataset(fp32titles);
        INT8Dataset int8titles = new INT8Dataset(fp32titles);
        INT4Dataset int4titles = new INT4Dataset(fp32titles);

        System.out.println("Micro Benchmark: Sum of dot products");
        try(Stopwatch ignore = new Stopwatch()) {
            System.out.print("FP32, " + fp32titles.matMul(queries) + ", ");
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("BF16, " + bFloat16titles.matMul(queries) + ", ");
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("INT8, " + int8titles.matMul(queries) + ", ");
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("INT4, " + int4titles.matMul(queries) + ", ");
        }

        System.out.println("Practical Benchmark: Measure recall@20");
        List<Set<Integer>> expects;
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("FP32, (1.0), ");
            expects = fp32titles.top(20, queries);
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("BF16, " + bFloat16titles.top(20, queries, expects) + ", ");
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("INT8, " + int8titles.top(20, queries, expects) + ", ");
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.print("INT4, " + int4titles.top(20, queries, expects) + ", ");
        }
    }
}
