import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

public class Main {
    public static float dotProduct(float[] a, float[] b) {
        assert a.length == b.length;
        float result = 0f;
        for (int i = 0; i < a.length; ++i) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static float dotProduct(float[] a, byte[] b) {
        assert a.length == b.length;
        float result = 0f;
        for (int i = 0; i < a.length; ++i) {
            result += a[i] * b[i];
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/owner/working/tmanabe/arbitrary-embeddings/jp_test.safetensors");
        Safetensors safetensors = Safetensors.safe_open(file);

        List<Integer> queriesShape = safetensors.getShape("queries");
        assert 2 == queriesShape.size();
        float[][] rawQueries = new float[queriesShape.get(0)][queriesShape.get(1)];
        {
            FloatBuffer queries = safetensors.getFP32("queries");
            for (int i = 0; i < queriesShape.get(0); ++i) {
                queries.get(rawQueries[i]);
            }
        }
        List<Integer> productTitlesShape = safetensors.getShape("product_titles");
        assert 2 == productTitlesShape.size();
        float[][] rawProductTitles = new float[productTitlesShape.get(0)][productTitlesShape.get(1)];
        {
            FloatBuffer productTitles = safetensors.getFP32("product_titles");
            for (int i = 0; i < productTitlesShape.get(0); ++i) {
                productTitles.get(rawProductTitles[i]);
            }
        }
        {
            long begin = System.currentTimeMillis();
            for (float[] rawQuery : rawQueries) {
                for (float[] rawProductTitle : rawProductTitles) {
                    dotProduct(rawQuery, rawProductTitle);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println(end - begin);
        }

        byte[][] int8ProductTitles = new byte[productTitlesShape.get(0)][productTitlesShape.get(1)];
        {
            for (int i = 0; i < productTitlesShape.get(0); ++i) {
                for (int j = 0; j < productTitlesShape.get(1); ++j) {
                    int8ProductTitles[i][j] = INT8.encode(rawProductTitles[i][j]);
                }
            }
        }
        {
            long begin = System.currentTimeMillis();
            for (float[] rawQuery : rawQueries) {
                for (byte[] int8ProductTitle : int8ProductTitles) {
                    dotProduct(rawQuery, int8ProductTitle);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println(end - begin);
        }
    }
}
