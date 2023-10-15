import java.io.Closeable;
import java.io.File;
import java.io.IOException;

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
        try(Stopwatch ignore = new Stopwatch()) {
            System.out.println(fp32_titles.matMul(queries));
        }
        INT8Dataset int8_titles = new INT8Dataset(fp32_titles);
        try(Stopwatch ignore = new Stopwatch()) {
            System.out.println(int8_titles.matMul(queries));
        }
    }
}
