import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Main {
    static class Stopwatch implements Closeable {
        Stopwatch() {
            begin = System.currentTimeMillis();
        }

        long begin;

        @Override
        public void close() {
            System.out.println(System.currentTimeMillis() - begin);
        }
    }

    static FP32Dataset prepare(Safetensors safetensors, String tensorName) {
        return new FP32Dataset(safetensors.getShape(tensorName), 1000, safetensors.getFP32(tensorName));
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/owner/working/tmanabe/arbitrary-embeddings/jp_test.safetensors");
        Safetensors safetensors = Safetensors.safe_open(file);
        FP32Dataset queries = prepare(safetensors, "queries");
        FP32Dataset fp32_titles = prepare(safetensors, "product_titles");

        System.out.println("Micro Benchmark:");
        try(Stopwatch ignore = new Stopwatch()) {
            System.out.println(fp32_titles.matMul(queries));
        }
        BFloat16Dataset bfloat16_titles = new BFloat16Dataset(fp32_titles);
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.println(bfloat16_titles.matMul(queries));
        }
        INT8Dataset int8_titles = new INT8Dataset(fp32_titles);
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.println(int8_titles.matMul(queries));
        }
        INT4Dataset int4_titles = new INT4Dataset(fp32_titles);
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.println(int4_titles.matMul(queries));
        }

        System.out.println("Practical Benchmark:");
        List<Set<Integer>> expects;
        try (Stopwatch ignore = new Stopwatch()) {
            expects = fp32_titles.top(20, queries);
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.println(bfloat16_titles.top(20, queries, expects));
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.println(int8_titles.top(20, queries, expects));
        }
        try (Stopwatch ignore = new Stopwatch()) {
            System.out.println(int4_titles.top(20, queries, expects));
        }
    }
}
